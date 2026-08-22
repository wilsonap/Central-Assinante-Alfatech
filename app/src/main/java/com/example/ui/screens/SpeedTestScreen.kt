package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.offline.OfflineStartup
import com.example.speedtest.SpeedTestHosts

/**
 * WebView dedicada ao Speedtest Custom — isolada da Central IXC.
 *
 * Necessário (Ookla STC): JavaScript, DOM Storage, rede HTTPS.
 * Não habilita: geolocalização Android, WebRTC especial, mixed content, file access.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var showOffline by remember {
        mutableStateOf(!OfflineStartup.isNetworkAvailable(context))
    }
    var loadEpoch by remember { mutableStateOf(0) }

    fun reload() {
        if (!OfflineStartup.isNetworkAvailable(context)) {
            showOffline = true
            isLoading = false
            return
        }
        showOffline = false
        isLoading = true
        progress = 0f
        loadEpoch++
    }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onClose()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            webViewRef = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        val wv = webViewRef
                        if (wv != null && wv.canGoBack()) {
                            wv.goBack()
                        } else {
                            onClose()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Teste de velocidade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                actions = {
                    IconButton(onClick = { reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showOffline) {
                OfflinePane(onRetry = { reload() })
            } else {
                SpeedTestWebView(
                    loadEpoch = loadEpoch,
                    onWebViewReady = { webViewRef = it },
                    onProgress = { p ->
                        progress = p
                        isLoading = p in 0f..<1f
                    },
                    onPageError = {
                        if (!OfflineStartup.isNetworkAvailable(context)) {
                            showOffline = true
                        }
                    },
                    openExternal = { uri ->
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflinePane(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sem conexão com a internet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Conecte-se à rede para medir a velocidade.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Tentar novamente")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SpeedTestWebView(
    loadEpoch: Int,
    onWebViewReady: (WebView) -> Unit,
    onProgress: (Float) -> Unit,
    onPageError: () -> Unit,
    openExternal: (Uri) -> Unit
) {
    val lastLoadedEpoch = remember { mutableStateOf(-1) }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    allowFileAccess = false
                    allowContentAccess = false
                    setSupportMultipleWindows(false)
                    mediaPlaybackRequiresUserGesture = true
                    // Geolocalização: sem permissão Android; prompts do site são bloqueados.
                    setGeolocationEnabled(false)
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgress(newProgress / 100f)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url ?: return true
                        val host = uri.host
                        return if (SpeedTestHosts.isAllowedHost(host)) {
                            false
                        } else {
                            openExternal(uri)
                            true
                        }
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onProgress(0f)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onProgress(1f)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            onPageError()
                        }
                    }
                }
                onWebViewReady(this)
                lastLoadedEpoch.value = loadEpoch
                loadUrl(SpeedTestHosts.START_URL)
            }
        },
        update = { view ->
            if (loadEpoch != lastLoadedEpoch.value) {
                lastLoadedEpoch.value = loadEpoch
                view.loadUrl(SpeedTestHosts.START_URL)
            }
        }
    )
}
