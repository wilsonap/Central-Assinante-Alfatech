package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.battery.BatteryOptimizationAssistant
import com.example.invoice.InvoiceReminderChecker
import com.example.notifications.NotificationChannels
import com.example.offline.OfflineStartup
import com.example.ui.MainViewModel
import com.example.ui.WhatsAppSupport
import com.example.ui.components.CentralWebView
import com.example.ui.components.CompanyLogoImage
import com.example.ui.screens.BatteryOptimizationPromptDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NeedsFirstOnlineAuthScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReceiptSenderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.update.InAppUpdateCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var activeWebView: WebView? = null
    private lateinit var inAppUpdateCoordinator: InAppUpdateCoordinator
    /** Evita múltiplos checks no mesmo ciclo de Activity (onStart repetido). */
    private val invoiceImmediateCheckStarted = AtomicBoolean(false)

    /**
     * Activity-level back callback kept at the top of the dispatcher so Chromium WebView
     * cannot swallow KEYCODE_BACK before navigateToHome() runs.
     */
    private val centralBackCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleCentralOrSystemBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Context válido somente após super.onCreate — evita NPE em AppUpdateManagerFactory.
        inAppUpdateCoordinator = InAppUpdateCoordinator(this)
        inAppUpdateCoordinator.onCreate()
        Log.i("APP_UPDATE", "coordinator_initialized=true")

        enableEdgeToEdge()

        // Enable global cookies for auto-login persistence
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)

        NotificationChannels.ensureCreated(this)

        handleNotificationIntent(intent)

        // Register early; reassertBackCallback() re-adds after WebView attaches/navigates.
        onBackPressedDispatcher.addCallback(this, centralBackCallback)

        setContent {
            MyApplicationTheme {
                AlfatechMainApp(
                    viewModel = viewModel,
                    onWebViewCreated = { webView ->
                        activeWebView = webView
                        reassertBackCallback()
                    },
                    onRefreshWeb = { activeWebView?.reload() },
                    canWebViewGoBack = { activeWebView?.canGoBack() == true },
                    webViewGoBack = { activeWebView?.goBack() },
                    onReassertBack = { reassertBackCallback() }
                )
            }
        }

        // AlarmManager é o principal: app_start NÃO posta day_before/due_date.
        if (invoiceImmediateCheckStarted.compareAndSet(false, true)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    InvoiceReminderChecker.run(this@MainActivity, trigger = "app_start")
                } catch (_: Exception) {
                    // Não bloqueia a UI.
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // In-App Update flexível: só com app aberto (fase 1).
        if (::inAppUpdateCoordinator.isInitialized) {
            inAppUpdateCoordinator.checkOnForeground()
        }
    }

    override fun onDestroy() {
        if (::inAppUpdateCoordinator.isInitialized) {
            inAppUpdateCoordinator.onDestroy()
        }
        super.onDestroy()
    }

    /** Puts our back callback above WebView's OnBackInvokedCallback (LIFO). */
    fun reassertBackCallback() {
        centralBackCallback.remove()
        onBackPressedDispatcher.addCallback(this, centralBackCallback)
    }

    private fun handleCentralOrSystemBack() {
        if (viewModel.needsFirstOnlineAuth.value) {
            Log.i("CENTRAL_NAV", "Back action=finish (needsFirstOnlineAuth)")
            finish()
            return
        }
        if (viewModel.showReceiptHistory.value) {
            Log.i("CENTRAL_NAV", "Back action=closeReceiptHistory")
            viewModel.closeReceiptHistory()
            return
        }
        if (viewModel.showReceiptSender.value) {
            Log.i("CENTRAL_NAV", "Back action=closeReceiptSender")
            viewModel.closeReceiptSender()
            return
        }

        val screen = viewModel.currentScreen.value
        val url = viewModel.currentUrl.value
        val canGoBack = activeWebView?.canGoBack() == true
        val screenLabel = when (screen) {
            1 -> "CENTRAL"
            2 -> "INVOICES"
            else -> "HOME"
        }
        Log.i(
            "CENTRAL_NAV",
            "Back pressed screen=$screenLabel url=$url canGoBack=$canGoBack"
        )

        when (screen) {
            2 -> {
                Log.i("CENTRAL_NAV", "Back action=navigateToHome from INVOICES")
                viewModel.navigateToHome()
            }
            1 -> {
                if (viewModel.isAtCentralRoot() || !canGoBack) {
                    Log.i("CENTRAL_NAV", "Back action=navigateToHome")
                    viewModel.navigateToHome()
                } else {
                    Log.i("CENTRAL_NAV", "Back action=webView.goBack()")
                    activeWebView?.goBack()
                }
                reassertBackCallback()
            }
            else -> {
                Log.i("CENTRAL_NAV", "Back action=finish (HOME)")
                finish()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Lê extras de toque na bandeja (FCM system ou PendingIntent local)
     * e persiste em Avisos quando houver title/body no data.
     */
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val extras = intent.extras ?: return

        val keys = extras.keySet().sorted().joinToString(",")
        android.util.Log.i("FCM_INTENT_EXTRAS", "keys=[$keys]")

        val title = extras.getString("title")
            ?: extras.getString("gcm.notification.title")
        val body = extras.getString("body")
            ?: extras.getString("message")
            ?: extras.getString("gcm.notification.body")
        val type = extras.getString("type")
        val targetUrl = extras.getString("target_url")
            ?: extras.getString("url")
        val messageId = extras.getString("google.message_id")
            ?: extras.getString("message_id")

        android.util.Log.i(
            "FCM_INTENT_EXTRAS",
            "title=${!title.isNullOrBlank()} body=${!body.isNullOrBlank()} " +
                "type=$type messageId=$messageId hasUrl=${!targetUrl.isNullOrBlank()}"
        )

        if (extras.getBoolean("open_invoices", false)) {
            viewModel.navigateToInvoices()
        }

        if (!targetUrl.isNullOrBlank()) {
            viewModel.setTargetUrl(targetUrl)
        }

        viewModel.persistPushFromIntentExtras(
            title = title,
            body = body,
            type = type,
            messageId = messageId,
            targetUrl = targetUrl
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlfatechMainApp(
    viewModel: MainViewModel,
    onWebViewCreated: (WebView) -> Unit,
    onRefreshWeb: () -> Unit,
    @Suppress("UNUSED_PARAMETER") canWebViewGoBack: () -> Boolean,
    @Suppress("UNUSED_PARAMETER") webViewGoBack: () -> Unit,
    onReassertBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val fcmToken by viewModel.fcmToken.collectAsState()
    val currentUrl by viewModel.currentUrl.collectAsState()
    val currentTitle by viewModel.currentTitle.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val showReceiptSender by viewModel.showReceiptSender.collectAsState()
    val showReceiptHistory by viewModel.showReceiptHistory.collectAsState()
    val receiptHistory by viewModel.receiptHistory.collectAsState()
    val receiptStorageCount by viewModel.receiptStorageCount.collectAsState()
    val receiptStorageBytes by viewModel.receiptStorageBytes.collectAsState()
    val clientFullName by viewModel.clientFullName.collectAsState()
    val clientCode by viewModel.clientCode.collectAsState()
    val clientContract by viewModel.clientContract.collectAsState()
    val supportWhatsAppNumber by viewModel.supportWhatsAppNumber.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val invoicesLastSyncedAt by viewModel.invoicesLastSyncedAt.collectAsState()
    val remindDayBefore by viewModel.remindDayBefore.collectAsState()
    val remindDueDate by viewModel.remindDueDate.collectAsState()
    val companyLogoUrl by viewModel.companyLogoUrl.collectAsState()
    val needsFirstOnlineAuth by viewModel.needsFirstOnlineAuth.collectAsState()
    val centralReloadRequest by viewModel.centralReloadRequest.collectAsState()
    val autoInvoiceSyncSignal by viewModel.autoInvoiceSyncSignal.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showBatteryOptPrompt by remember { mutableStateOf(false) }

    // Request Android 13+ Notification Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        BatteryOptimizationAssistant.logStatus(context)
        if (BatteryOptimizationAssistant.shouldShowPrompt(context)) {
            showBatteryOptPrompt = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (showBatteryOptPrompt) {
        BatteryOptimizationPromptDialog(
            onAllowBackground = {
                BatteryOptimizationAssistant.markPromptAcknowledged(context)
                showBatteryOptPrompt = false
                BatteryOptimizationAssistant.openBackgroundSettings(context)
            },
            onDismiss = {
                BatteryOptimizationAssistant.markPromptAcknowledged(context)
                showBatteryOptPrompt = false
            }
        )
    }

    // Ao abrir Avisos e Comunicados, zera o badge do sino
    LaunchedEffect(showNotificationsSheet) {
        if (showNotificationsSheet) {
            viewModel.markAllNotificationsAsRead()
        }
    }

    // Keep Activity back callback above WebView's after URL/screen changes
    LaunchedEffect(currentScreen, currentUrl) {
        onReassertBack()
    }

    // Ao voltar a internet após startup offline: recarrega Central (sessão/cookies intactos).
    LaunchedEffect(centralReloadRequest) {
        if (centralReloadRequest > 0L) {
            onRefreshWeb()
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    if (needsFirstOnlineAuth) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                NeedsFirstOnlineAuthScreen(onRetry = { viewModel.retryFirstOnlineAuth() })
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isLoggedIn) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    if (currentScreen == 0 || currentScreen == 2) {
                        // Home / Faturas nativas
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White
                            ),
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CompanyLogoImage(
                                        localLogoPath = companyLogoUrl,
                                        size = 48.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentScreen == 2) {
                                                "Faturas"
                                            } else {
                                                "Alfatech Telecom"
                                            },
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (currentScreen == 2) {
                                                "Salvas no aparelho"
                                            } else {
                                                "Central do Assinante"
                                            },
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                if (currentScreen == 2) {
                                    IconButton(onClick = { viewModel.navigateToHome() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Voltar"
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { showNotificationsSheet = true }) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge { Text(unreadCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Avisos"
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        // WebView Header Discreto
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            ),
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        android.util.Log.i(
                                            "CENTRAL_NAV",
                                            "Blue arrow action=navigateToHome screen=CENTRAL url=$currentUrl"
                                        )
                                        viewModel.navigateToHome()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Voltar"
                                    )
                                }
                            },
                            title = {
                                Text(
                                    text = currentTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                IconButton(onClick = onRefreshWeb) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Atualizar página"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (isLoggedIn) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Início
                    NavigationBarItem(
                        selected = currentScreen == 0,
                        onClick = { viewModel.selectBottomTab(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Faturas (lista offline Room)
                    NavigationBarItem(
                        selected = currentScreen == 2 || (currentScreen == 1 && selectedTab == 1),
                        onClick = { viewModel.selectBottomTab(1) },
                        icon = { Icon(Icons.Default.Receipt, contentDescription = "Faturas") },
                        label = { Text("Faturas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Suporte
                    NavigationBarItem(
                        selected = currentScreen == 1 && selectedTab == 2,
                        onClick = { viewModel.selectBottomTab(2) },
                        icon = { Icon(Icons.Default.HeadsetMic, contentDescription = "Suporte") },
                        label = { Text("Suporte", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    // Perfil
                    NavigationBarItem(
                        selected = currentScreen == 1 && selectedTab == 3,
                        onClick = { viewModel.selectBottomTab(3) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Keep CentralWebView in Composition so session/cookies/JS remain active continuously.
            // Hide its native surface while Home is active to avoid z-order / empty-home bugs.
            CentralWebView(
                url = currentUrl,
                fcmToken = fcmToken,
                isVisible = currentScreen == 1 && !needsFirstOnlineAuth,
                onWebViewCreated = onWebViewCreated,
                onTitleChanged = { newTitle ->
                    viewModel.updateWebTitle(newTitle)
                },
                onUrlChanged = { newUrl ->
                    viewModel.onUrlChanged(newUrl)
                },
                onWhatsAppConfigFound = { number, message, fullUrl, source ->
                    viewModel.updateWhatsAppConfig(number, message, fullUrl, source)
                },
                onClientProfileFound = { fullName, code, contract ->
                    viewModel.updateClientProfile(fullName, code, contract)
                },
                onPostLoginReady = { trigger ->
                    viewModel.onPostLoginReady(trigger)
                },
                onInvoicesJsonReceived = { json ->
                    viewModel.onInvoicesJsonCaptured(json)
                },
                onCompanyLogoFound = { logoUrl ->
                    viewModel.onCompanyLogoCaptured(logoUrl)
                },
                autoInvoiceSyncSignal = autoInvoiceSyncSignal,
                onAutoInvoiceSyncStarted = { trigger ->
                    viewModel.onAutoInvoiceSyncStarted(trigger)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
            )

            // Render Native Home Screen on top when currentScreen == 0
            if (currentScreen == 0 && !showReceiptSender && !showReceiptHistory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    HomeScreen(
                        onNavigateToUrl = { path, title ->
                            viewModel.navigateToShortcut(path, title)
                        },
                        onWhatsAppClick = {
                            if (!OfflineStartup.isNetworkAvailable(context)) {
                                Toast.makeText(context, "Sem conexão", Toast.LENGTH_SHORT).show()
                            } else {
                                val message = WhatsAppSupport.buildSupportMessage(
                                    fullName = clientFullName,
                                    clientCode = clientCode,
                                    contract = clientContract
                                )
                                WhatsAppSupport.openChat(
                                    context = context,
                                    number = viewModel.supportWhatsAppNumber.value,
                                    message = message,
                                    fullUrl = viewModel.supportWhatsAppUrl.value
                                )
                            }
                        },
                        onReceiptClick = { viewModel.openReceiptSender() },
                        onReceiptHistoryClick = { viewModel.openReceiptHistory() }
                    )
                }
            }

            if (currentScreen == 2 && !showReceiptSender && !showReceiptHistory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    com.example.ui.screens.InvoicesScreen(
                        invoices = invoices,
                        lastSyncedAt = invoicesLastSyncedAt,
                        remindDayBefore = remindDayBefore,
                        remindDueDate = remindDueDate,
                        onRemindDayBeforeChange = { viewModel.setRemindDayBefore(it) },
                        onRemindDueDateChange = { viewModel.setRemindDueDate(it) },
                        onOpenCentralInvoices = { viewModel.openCentralInvoices() }
                    )
                }
            }

            if (showReceiptSender) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ReceiptSenderScreen(
                        clientFullName = clientFullName,
                        clientCode = clientCode,
                        clientContract = clientContract,
                        supportWhatsAppNumber = supportWhatsAppNumber,
                        onClose = { viewModel.closeReceiptSender() }
                    )
                }
            }

            if (showReceiptHistory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    com.example.ui.screens.ReceiptHistoryScreen(
                        receipts = receiptHistory,
                        supportWhatsAppNumber = supportWhatsAppNumber,
                        storageCount = receiptStorageCount,
                        storageBytes = receiptStorageBytes,
                        onClose = { viewModel.closeReceiptHistory() },
                        onDeleted = { viewModel.refreshReceiptStorageStats() }
                    )
                }
            }
        }

        if (showNotificationsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationsSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                NotificationsScreen(
                    notifications = notifications,
                    onClearAll = { viewModel.clearAllNotifications() },
                    onNotificationClick = { notification ->
                        showNotificationsSheet = false
                        viewModel.onNotificationHistoryClick(notification)
                    }
                )
            }
        }
    }
}
