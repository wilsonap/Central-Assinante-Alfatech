package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationEntity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val notificationDao = db.notificationDao()

    val notifications: StateFlow<List<NotificationEntity>> = notificationDao.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _fcmToken = MutableStateFlow("")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    // 0 = Home Screen (Native), 1 = WebView Screen
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

    init {
        retrieveFcmToken()
    }

    fun onUrlChanged(newUrl: String) {
        _currentUrl.value = newUrl
        val authenticatedPaths = listOf(
            "dados_cliente", "faturas", "planos", "consumos",
            "relatorios", "atendimentos", "configuracoes", "principal", "home", "painel", "dashboard"
        )
        if (authenticatedPaths.any { newUrl.contains(it, ignoreCase = true) }) {
            _isLoggedIn.value = true
        }
    }

    fun selectBottomTab(tabIndex: Int) {
        _isLoggedIn.value = true
        _selectedTab.value = tabIndex
        when (tabIndex) {
            0 -> {
                _currentScreen.value = 0
            }
            1 -> {
                _currentScreen.value = 1
                _currentUrl.value = "${baseUrl}faturas"
                _currentTitle.value = "Faturas"
            }
            2 -> {
                _currentScreen.value = 1
                _currentUrl.value = "${baseUrl}atendimentos"
                _currentTitle.value = "Suporte / Atendimento"
            }
            3 -> {
                _currentScreen.value = 1
                _currentUrl.value = "${baseUrl}dados_cliente"
                _currentTitle.value = "Meus Dados"
            }
        }
    }

    fun navigateToShortcut(path: String, title: String) {
        _isLoggedIn.value = true
        val targetUrl = if (path.isBlank()) baseUrl else if (path.startsWith("http")) path else "${baseUrl}$path"
        _currentUrl.value = targetUrl
        _currentTitle.value = title
        _currentScreen.value = 1

        when (path) {
            "faturas" -> _selectedTab.value = 1
            "atendimentos" -> _selectedTab.value = 2
            "dados_cliente" -> _selectedTab.value = 3
            else -> _selectedTab.value = -1
        }
    }

    fun navigateToHome() {
        _currentScreen.value = 0
        _selectedTab.value = 0
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

    private fun retrieveFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    _fcmToken.value = token
                    android.util.Log.i("AlfatechFCM", "FCM TOKEN: $token")
                    android.util.Log.i("AlfatechFCM", "FCM TOKEN NOVO APP: $token")
                } else {
                    val exception = task.exception
                    android.util.Log.e("AlfatechFCM", "Erro ao obter token FCM", exception)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AlfatechFCM", "Exceção ao obter token FCM", e)
        }
    }
}
