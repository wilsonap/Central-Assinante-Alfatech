package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.components.CentralWebView
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var activeWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable global cookies for auto-login persistence
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)

        // Create Default FCM Notification Channel
        createNotificationChannel()

        intent?.getStringExtra("target_url")?.let { targetUrl ->
            viewModel.setTargetUrl(targetUrl)
        }

        setContent {
            MyApplicationTheme {
                AlfatechMainApp(
                    viewModel = viewModel,
                    onWebViewCreated = { webView -> activeWebView = webView },
                    onRefreshWeb = { activeWebView?.reload() },
                    canWebViewGoBack = { activeWebView?.canGoBack() == true },
                    webViewGoBack = { activeWebView?.goBack() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("target_url")?.let { targetUrl ->
            viewModel.setTargetUrl(targetUrl)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val channelDesc = getString(R.string.default_notification_channel_desc)
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, channelName, importance).apply {
                description = channelDesc
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlfatechMainApp(
    viewModel: MainViewModel,
    onWebViewCreated: (WebView) -> Unit,
    onRefreshWeb: () -> Unit,
    canWebViewGoBack: () -> Boolean,
    webViewGoBack: () -> Unit
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val fcmToken by viewModel.fcmToken.collectAsState()
    val currentUrl by viewModel.currentUrl.collectAsState()
    val currentTitle by viewModel.currentTitle.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }

    // Request Android 13+ Notification Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
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

    // Handle physical / gesture back button
    BackHandler(enabled = true) {
        if (currentScreen == 1) {
            if (canWebViewGoBack()) {
                webViewGoBack()
            } else {
                viewModel.navigateToHome()
            }
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isLoggedIn) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    if (currentScreen == 0) {
                        // Home Screen Top App Bar
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White
                            ),
                            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Alfatech Telecom",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Central do Assinante",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
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
                                        if (canWebViewGoBack()) {
                                            webViewGoBack()
                                        } else {
                                            viewModel.navigateToHome()
                                        }
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

                    // Faturas
                    NavigationBarItem(
                        selected = currentScreen == 1 && selectedTab == 1,
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
            // Keep CentralWebView in Composition so session/cookies/JS remain active continuously
            CentralWebView(
                url = currentUrl,
                fcmToken = fcmToken,
                onWebViewCreated = onWebViewCreated,
                onTitleChanged = { newTitle ->
                    viewModel.updateWebTitle(newTitle)
                },
                onUrlChanged = { newUrl ->
                    viewModel.onUrlChanged(newUrl)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Render Native Home Screen on top when currentScreen == 0
            if (currentScreen == 0) {
                HomeScreen(
                    onNavigateToUrl = { path, title ->
                        viewModel.navigateToShortcut(path, title)
                    }
                )
            }
        }

        if (showNotificationsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationsSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                NotificationsScreen(
                    notifications = notifications,
                    onClearAll = { viewModel.clearAllNotifications() }
                )
            }
        }
    }
}
