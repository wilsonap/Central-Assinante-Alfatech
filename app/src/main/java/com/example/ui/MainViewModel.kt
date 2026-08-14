package com.example.ui

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PushNotificationRepository
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import com.example.data.local.NotificationEntity
import com.example.invoice.InvoiceReminderPrefs
import com.example.invoice.InvoiceRepository
import com.example.offline.OfflineStartup
import com.example.service.FcmTokenStore
import com.example.ui.components.FcmBridgeController
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val notificationDao = db.notificationDao()
    private val receiptHistoryDao = db.receiptHistoryDao()
    private val invoiceRepository = InvoiceRepository(application)

    val notifications: StateFlow<List<NotificationEntity>> = notificationDao.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val receiptHistory: StateFlow<List<com.example.data.local.ReceiptHistoryEntity>> =
        receiptHistoryDao.observeAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val invoices: StateFlow<List<InvoiceEntity>> = invoiceRepository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _invoicesLastSyncedAt = MutableStateFlow<Long?>(null)
    val invoicesLastSyncedAt: StateFlow<Long?> = _invoicesLastSyncedAt.asStateFlow()

    val remindDayBefore: StateFlow<Boolean> = InvoiceReminderPrefs.remindDayBefore
    val remindDueDate: StateFlow<Boolean> = InvoiceReminderPrefs.remindDueDate

    private val _receiptStorageCount = MutableStateFlow(0)
    val receiptStorageCount: StateFlow<Int> = _receiptStorageCount.asStateFlow()

    private val _receiptStorageBytes = MutableStateFlow(0L)
    val receiptStorageBytes: StateFlow<Long> = _receiptStorageBytes.asStateFlow()

    private val _fcmToken = MutableStateFlow(FcmTokenStore.current(application))
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _supportWhatsAppNumber = MutableStateFlow("")
    val supportWhatsAppNumber: StateFlow<String> = _supportWhatsAppNumber.asStateFlow()

    private val _supportWhatsAppMessage = MutableStateFlow(WhatsAppSupport.DEFAULT_MESSAGE)
    val supportWhatsAppMessage: StateFlow<String> = _supportWhatsAppMessage.asStateFlow()

    private val _supportWhatsAppUrl = MutableStateFlow("")
    val supportWhatsAppUrl: StateFlow<String> = _supportWhatsAppUrl.asStateFlow()

    private val _clientFullName = MutableStateFlow("")
    val clientFullName: StateFlow<String> = _clientFullName.asStateFlow()

    private val _clientCode = MutableStateFlow("")
    val clientCode: StateFlow<String> = _clientCode.asStateFlow()

    private val _clientContract = MutableStateFlow("")
    val clientContract: StateFlow<String> = _clientContract.asStateFlow()

    private val _showReceiptSender = MutableStateFlow(false)
    val showReceiptSender: StateFlow<Boolean> = _showReceiptSender.asStateFlow()

    private val _showReceiptHistory = MutableStateFlow(false)
    val showReceiptHistory: StateFlow<Boolean> = _showReceiptHistory.asStateFlow()


    // 0 = Home, 1 = WebView Central, 2 = Faturas nativas (Room)
    private val _currentScreen = MutableStateFlow(1)
    val currentScreen: StateFlow<Int> = _currentScreen.asStateFlow()

    // 0 = Início, 1 = Faturas, 2 = Suporte, 3 = Perfil (-1 if custom shortcut)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val baseUrl = "https://sac2.alfatechtelecom.com.br/central_assinante_web/"

    private val _currentUrl = MutableStateFlow(baseUrl)
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _currentTitle = MutableStateFlow("Central do Assinante")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /** Offline sem nenhuma sessão/dado local prévio — não entra em loop na WebView. */
    private val _needsFirstOnlineAuth = MutableStateFlow(false)
    val needsFirstOnlineAuth: StateFlow<Boolean> = _needsFirstOnlineAuth.asStateFlow()

    /** Pedido para recarregar a Central em background ao voltar a internet. */
    private val _centralReloadRequest = MutableStateFlow(0L)
    val centralReloadRequest: StateFlow<Long> = _centralReloadRequest.asStateFlow()

    /** Pedido de sync automático de faturas (id + trigger). */
    private val _autoInvoiceSyncSignal = MutableStateFlow<Pair<Long, String>?>(null)
    val autoInvoiceSyncSignal: StateFlow<Pair<Long, String>?> = _autoInvoiceSyncSignal.asStateFlow()

    private var pendingAutoInvoiceSyncTrigger: String? = null

    private var startedOfflineWithLocalData = false

    /**
     * Evita loop: Home automática só na transição não autenticado → autenticado,
     * uma vez por ciclo de sessão. Reset ao voltar para tela de login IXC.
     */
    private val postLoginHomeHandled = AtomicBoolean(false)

    private val connectivityManager =
        application.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            viewModelScope.launch(Dispatchers.Main) {
                onNetworkBecameAvailable()
            }
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                viewModelScope.launch(Dispatchers.Main) {
                    onNetworkBecameAvailable()
                }
            }
        }
    }

    init {
        InvoiceReminderPrefs.hydrate(application)
        retrieveFcmToken()
        refreshReceiptStorageStats()
        refreshInvoicesLastSync()
        evaluateOfflineStartup()
        registerNetworkCallback()
    }

    override fun onCleared() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
        super.onCleared()
    }

    /**
     * Se offline com dados/sessão local → Home nativa.
     * Se offline sem histórico → aviso de primeira autenticação.
     * Online → fluxo WebView/login atual (não altera).
     */
    private fun evaluateOfflineStartup() {
        val app = getApplication<Application>()
        val networkAvailable = OfflineStartup.isNetworkAvailable(app)
        val cookieSession = OfflineStartup.hasCookieSession(baseUrl)
        val hadAuth = OfflineStartup.hadAuthenticatedSession(app)
        val localSessionAvailable = cookieSession || hadAuth
        val (invoiceCount, receiptCount) = runBlocking(Dispatchers.IO) {
            db.invoiceDao().count() to receiptHistoryDao.count()
        }
        val localDataAvailable =
            invoiceCount > 0 || receiptCount > 0 || localSessionAvailable

        if (!networkAvailable && localDataAvailable) {
            startedOfflineWithLocalData = true
            _needsFirstOnlineAuth.value = false
            _isLoggedIn.value = true
            postLoginHomeHandled.set(true)
            _currentScreen.value = 0
            _selectedTab.value = 0
            OfflineStartup.logDecision(
                networkAvailable = false,
                localSessionAvailable = localSessionAvailable,
                localDataAvailable = true,
                navigateHomeOffline = true
            )
            return
        }

        if (!networkAvailable && !localDataAvailable) {
            _needsFirstOnlineAuth.value = true
            _isLoggedIn.value = false
            _currentScreen.value = 0
            OfflineStartup.logDecision(
                networkAvailable = false,
                localSessionAvailable = false,
                localDataAvailable = false,
                navigateHomeOffline = false
            )
            return
        }

        OfflineStartup.logDecision(
            networkAvailable = true,
            localSessionAvailable = localSessionAvailable,
            localDataAvailable = localDataAvailable,
            navigateHomeOffline = false
        )
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
        }
    }

    private fun onNetworkBecameAvailable() {
        if (_needsFirstOnlineAuth.value) {
            // Usuário ainda na tela de "precisa autenticar uma vez" — não forçar login automático.
            return
        }
        val app = getApplication<Application>()
        val hasSession = OfflineStartup.hasCookieSession(baseUrl) ||
            OfflineStartup.hadAuthenticatedSession(app)
        if (startedOfflineWithLocalData) {
            startedOfflineWithLocalData = false
            android.util.Log.i("OFFLINE_STARTUP", "networkAvailable=true recovery=reload_central")
            // Reutiliza cookies/sessão; sync virá no session_restored após o reload.
            _centralReloadRequest.value = System.currentTimeMillis()
            return
        }
        if (hasSession) {
            // App já estava em memória: sync forçado sem exigir abrir Faturas.
            requestAutoInvoiceSync("network_recovered")
        }
    }

    fun requestAutoInvoiceSync(trigger: String) {
        _autoInvoiceSyncSignal.value = System.currentTimeMillis() to trigger
    }

    fun onAutoInvoiceSyncStarted(trigger: String) {
        pendingAutoInvoiceSyncTrigger = trigger
    }

    /** Retry da tela de primeira autenticação quando o usuário recupera internet. */
    fun retryFirstOnlineAuth() {
        val online = OfflineStartup.isNetworkAvailable(getApplication())
        android.util.Log.i("OFFLINE_STARTUP", "networkAvailable=$online retryFirstOnlineAuth")
        if (!online) {
            Toast.makeText(
                getApplication(),
                "Sem conexão. Conecte-se à internet para autenticar.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        _needsFirstOnlineAuth.value = false
        _currentScreen.value = 1
        _currentUrl.value = baseUrl
        _centralReloadRequest.value = System.currentTimeMillis()
    }

    private fun requireOnlineForCentral(): Boolean {
        if (OfflineStartup.isNetworkAvailable(getApplication())) return true
        Toast.makeText(getApplication(), "Sem conexão", Toast.LENGTH_SHORT).show()
        return false
    }

    fun onUrlChanged(newUrl: String) {
        _currentUrl.value = newUrl
        // Offline: falha de rede NÃO invalida sessão local.
        if (!OfflineStartup.isNetworkAvailable(getApplication())) {
            return
        }
        if (FcmBridgeController.isAuthBlockedUrl(newUrl)) {
            pendingAutoInvoiceSyncTrigger = null
            if (postLoginHomeHandled.getAndSet(false)) {
                android.util.Log.i(
                    "CENTRAL_POST_LOGIN",
                    "auth_transition=authenticated_to_unauthenticated"
                )
            }
        }
        val authenticatedPaths = listOf(
            "dados_cliente", "faturas", "planos", "consumos",
            "relatorios", "atendimentos", "configuracoes", "principal", "home", "painel", "dashboard"
        )
        if (authenticatedPaths.any { newUrl.contains(it, ignoreCase = true) }) {
            _isLoggedIn.value = true
        }
    }

    /**
     * Após auth confirmada + refreshCentralCustomerData (ciclo #40).
     * Navega à Home nativa uma vez por sessão autenticada.
     */
    fun onPostLoginReady(trigger: String) {
        val authType = when (trigger) {
            "login_authenticated" -> "manual"
            "session_restored" -> "automatic"
            else -> trigger
        }
        android.util.Log.i("CENTRAL_POST_LOGIN", "auth_type=$authType")
        if (!postLoginHomeHandled.compareAndSet(false, true)) {
            android.util.Log.i("CENTRAL_POST_LOGIN", "navigate_skipped=already_handled")
            return
        }
        OfflineStartup.markAuthenticatedSession(getApplication())
        _needsFirstOnlineAuth.value = false
        _isLoggedIn.value = true
        android.util.Log.i("CENTRAL_POST_LOGIN", "navigate_to_home=true")
        navigateToHome()
    }

    fun updateWhatsAppConfig(number: String, message: String, fullUrl: String, source: String) {
        val digits = number.filter { it.isDigit() }
        if (digits.isEmpty() && fullUrl.isBlank()) return
        if (digits.isNotEmpty()) {
            _supportWhatsAppNumber.value = digits
        }
        if (message.isNotBlank()) {
            _supportWhatsAppMessage.value = message
        }
        if (fullUrl.isNotBlank()) {
            _supportWhatsAppUrl.value = fullUrl
        }
        if (digits.isNotEmpty()) {
            WhatsAppSupport.logCaptured(digits, source)
        }
    }

    fun updateClientFullName(fullName: String) {
        val name = fullName.trim()
        if (name.isBlank()) return
        _clientFullName.value = name
        android.util.Log.i("RECEIPT_SEND", "clientFullName captured=true")
    }

    fun updateClientProfile(fullName: String, code: String, contract: String) {
        val name = fullName.trim()
        val codigo = code.trim()
        val contrato = contract.trim()
        if (name.isNotBlank()) _clientFullName.value = name
        if (codigo.isNotBlank()) _clientCode.value = codigo
        if (contrato.isNotBlank()) _clientContract.value = contrato
        if (name.isNotBlank() || codigo.isNotBlank() || contrato.isNotBlank()) {
            android.util.Log.i(
                "RECEIPT_SEND",
                "clientProfile name=${name.isNotBlank()} code=${codigo.isNotBlank()} contract=${contrato.isNotBlank()}"
            )
        }
    }

    fun openReceiptSender() {
        _showReceiptSender.value = true
    }

    fun closeReceiptSender() {
        _showReceiptSender.value = false
    }

    fun openReceiptHistory() {
        refreshReceiptStorageStats()
        _showReceiptHistory.value = true
    }

    fun closeReceiptHistory() {
        _showReceiptHistory.value = false
    }

    fun refreshReceiptStorageStats() {
        viewModelScope.launch {
            val (count, bytes) = com.example.receipt.ReceiptHistoryStore.storageStats(getApplication())
            _receiptStorageCount.value = count
            _receiptStorageBytes.value = bytes
        }
    }

    fun selectBottomTab(tabIndex: Int) {
        _isLoggedIn.value = true
        _selectedTab.value = tabIndex
        when (tabIndex) {
            0 -> {
                navigateToHome()
            }
            1 -> {
                navigateToInvoices()
            }
            2 -> {
                openCentral("${baseUrl}atendimentos", "Suporte / Atendimento", selectedTabValue = 2)
            }
            3 -> {
                openCentral("${baseUrl}dados_cliente", "Meus Dados", selectedTabValue = 3)
            }
        }
    }

    fun navigateToInvoices() {
        _isLoggedIn.value = true
        _selectedTab.value = 1
        _currentScreen.value = 2
        _currentTitle.value = "Faturas"
        refreshInvoicesLastSync()
        android.util.Log.i("CENTRAL_NAV", "navigateToInvoices")
    }

    fun openCentralInvoices() {
        openCentral("${baseUrl}faturas", "Faturas", selectedTabValue = 1)
    }

    fun navigateToShortcut(path: String, title: String) {
        _isLoggedIn.value = true
        if (path == "faturas") {
            navigateToInvoices()
            return
        }
        val targetUrl = if (path.isBlank()) baseUrl else if (path.startsWith("http")) path else "${baseUrl}$path"
        val tab = when (path) {
            "atendimentos" -> 2
            "dados_cliente" -> 3
            else -> -1
        }
        openCentral(targetUrl, title, selectedTabValue = tab)
    }

    fun onInvoicesJsonCaptured(rawJson: String) {
        viewModelScope.launch {
            val autoTrigger = pendingAutoInvoiceSyncTrigger
            try {
                val count = invoiceRepository.syncFromGetFaturasJson(rawJson)
                if (autoTrigger != null) {
                    android.util.Log.i("INVOICE_AUTO_SYNC", "trigger=$autoTrigger")
                    android.util.Log.i("INVOICE_AUTO_SYNC", "syncFinished=true")
                    android.util.Log.i("INVOICE_AUTO_SYNC", "invoiceCount=$count")
                    pendingAutoInvoiceSyncTrigger = null
                }
                // Reagenda lembretes após sync bem-sucedida (mesmo com 0 itens novos).
                com.example.invoice.InvoiceReminderScheduler.schedulePeriodic(getApplication())
                withContext(Dispatchers.IO) {
                    com.example.invoice.InvoiceReminderChecker.run(
                        getApplication(),
                        trigger = "invoice_sync"
                    )
                }
                refreshInvoicesLastSync()
            } catch (e: Exception) {
                if (autoTrigger != null) {
                    android.util.Log.i("INVOICE_AUTO_SYNC", "trigger=$autoTrigger")
                    android.util.Log.i("INVOICE_AUTO_SYNC", "syncFailed=true")
                    pendingAutoInvoiceSyncTrigger = null
                }
                // Mantém Room antigo; não trata como logout.
            }
        }
    }

    fun setRemindDayBefore(enabled: Boolean) {
        InvoiceReminderPrefs.setRemindDayBefore(getApplication(), enabled)
    }

    fun setRemindDueDate(enabled: Boolean) {
        InvoiceReminderPrefs.setRemindDueDate(getApplication(), enabled)
    }

    private fun refreshInvoicesLastSync() {
        viewModelScope.launch {
            _invoicesLastSyncedAt.value = invoiceRepository.latestSyncAt()
        }
    }

    fun navigateToHome() {
        val prev = screenLabel(_currentScreen.value)
        _currentScreen.value = 0
        _selectedTab.value = 0
        android.util.Log.i("CENTRAL_NAV", "navigateToHome $prev -> HOME")
    }

    private fun openCentral(url: String, title: String, selectedTabValue: Int) {
        if (!requireOnlineForCentral()) return
        val prev = screenLabel(_currentScreen.value)
        _currentUrl.value = url
        _currentTitle.value = title
        _selectedTab.value = selectedTabValue
        _currentScreen.value = 1
        android.util.Log.i("CENTRAL_NAV", "openCentral $prev -> CENTRAL url=$url")
    }

    private fun screenLabel(screen: Int): String = when (screen) {
        1 -> "CENTRAL"
        2 -> "INVOICES"
        else -> "HOME"
    }

    /**
     * True when the WebView URL is the Central root (or logged-in landing),
     * so system Back should leave the WebView and restore the native Home.
     */
    fun isAtCentralRoot(): Boolean {
        val raw = _currentUrl.value
            .substringBefore('#')
            .substringBefore('?')
            .trimEnd('/')
        val root = baseUrl.trimEnd('/')
        if (raw.equals(root, ignoreCase = true)) return true
        if (!raw.startsWith(root, ignoreCase = true)) return false
        val path = raw.substring(root.length).trimStart('/').lowercase()
        return path.isEmpty() || path in setOf(
            "principal", "home", "painel", "dashboard", "index"
        )
    }

    fun updateWebTitle(newTitle: String) {
        if (newTitle.isNotBlank() && _currentScreen.value == 1) {
            _currentTitle.value = newTitle
        }
    }

    fun setTargetUrl(url: String) {
        if (url.isNotEmpty()) {
            _currentUrl.value = url
            _currentScreen.value = 1
            if (url.contains("faturas")) _selectedTab.value = 1
            else if (url.contains("atendimentos")) _selectedTab.value = 2
            else if (url.contains("dados_cliente")) _selectedTab.value = 3
            else _selectedTab.value = -1
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationDao.clearAll()
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    /**
     * Persiste push aberto a partir da bandeja (extras do Intent).
     * Sem title/body nos extras (notification-only), não inventa conteúdo.
     */
    fun persistPushFromIntentExtras(
        title: String?,
        body: String?,
        type: String?,
        messageId: String?,
        targetUrl: String?
    ) {
        if (title.isNullOrBlank() && body.isNullOrBlank()) {
            android.util.Log.w(
                "FCM_INTENT_EXTRAS",
                "sem title/body nos extras — provavelmente notification-only; " +
                    "servidor precisa enviar data.title/data.body/data.type/data.url/data.message_id"
            )
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                PushNotificationRepository.persistFromPush(
                    context = getApplication(),
                    title = title,
                    body = body,
                    type = type,
                    messageId = messageId,
                    targetUrl = targetUrl
                )
            }
        }
    }

    /** Atualiza estado Compose a partir do store (ex.: após onNewToken). */
    fun syncFcmTokenFromStore() {
        val token = FcmTokenStore.current(getApplication())
        if (token.isNotEmpty() && token != _fcmToken.value) {
            _fcmToken.value = token
        }
    }

    private fun retrieveFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    FcmTokenStore.update(getApplication(), token)
                    _fcmToken.value = token
                    android.util.Log.i(
                        "FCM_TOKEN_READY",
                        "token obtido no ViewModel mask=${FcmTokenStore.mask(token)}"
                    )
                } else {
                    android.util.Log.e("AlfatechFCM", "Erro ao obter token FCM", task.exception)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AlfatechFCM", "Exceção ao obter token FCM", e)
        }
    }
}
