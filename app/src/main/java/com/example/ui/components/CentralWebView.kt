package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CentralWebView(
    url: String,
    modifier: Modifier = Modifier,
    fcmToken: String = "",
    onWebViewCreated: (WebView) -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onUrlChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastRequestedUrl by remember { mutableStateOf("") }

    LaunchedEffect(url, webViewRef) {
        val wv = webViewRef
        if (wv != null && url.isNotEmpty() && url != lastRequestedUrl) {
            lastRequestedUrl = url
            isError = false
            isLoading = true
            android.util.Log.d("CentralAutoLogin", "[App Navigation] Solicitando carregamento explicito de URL: $url")
            wv.loadUrl(url)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Sem conexão",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sem conexão com a internet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verifique sua conexão e tente novamente.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isError = false
                            isLoading = true
                            webViewRef?.reload()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Tentar novamente")
                    }
                }
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        onWebViewCreated(this)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            
                            useWideViewPort = false
                            loadWithOverviewMode = false
                            textZoom = 100
                            
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            javaScriptCanOpenWindowsAutomatically = true
                            setSupportMultipleWindows(false)
                            mediaPlaybackRequiresUserGesture = false
                            cacheMode = WebSettings.LOAD_DEFAULT
                            
                            // Standard Chrome Android User-Agent for modern mobile web compatibility
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        }

                        android.util.Log.d("CentralAutoLogin", "User-Agent configurado: ${settings.userAgentString}")

                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocus()

                        // Configure CookieManager BEFORE first load
                        val webViewInstance = this
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setAcceptThirdPartyCookies(webViewInstance, true)
                        }

                        addJavascriptInterface(WebAppInterface(ctx, fcmToken), "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val reqUrl = request?.url?.toString() ?: return false
                                val isRedirect = request?.isRedirect == true
                                val isMainFrame = request?.isForMainFrame == true
                                android.util.Log.d("CentralAutoLogin", "-> [Redirect/Nav] Req: $reqUrl | Redirect: $isRedirect | MainFrame: $isMainFrame")

                                // Open WhatsApp or external non-http schemes in external apps
                                if (reqUrl.startsWith("whatsapp://") || reqUrl.contains("wa.me/") || reqUrl.startsWith("tel:") || reqUrl.startsWith("mailto:")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                                        ctx.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "Aplicativo não encontrado para abrir o link", Toast.LENGTH_SHORT).show()
                                        return true
                                    }
                                }

                                // Open PDF / Boleto downloads in external browser if required
                                if (reqUrl.endsWith(".pdf") || reqUrl.contains("download_boleto") || reqUrl.contains("gerar_boleto")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                                        ctx.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        // Fall through to load in WebView
                                    }
                                }

                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                val cookies = url?.let { CookieManager.getInstance().getCookie(it) } ?: "Nenhum"
                                android.util.Log.d("CentralAutoLogin", "-> [onPageStarted] URL: $url | Cookies: $cookies")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                isError = false
                                CookieManager.getInstance().flush()
                                val cookies = url?.let { CookieManager.getInstance().getCookie(it) } ?: "Nenhum"
                                android.util.Log.d("CentralAutoLogin", "-> [onPageFinished] URL Final: $url | Cookies Criados/Sessão: $cookies")
                                url?.let {
                                    lastRequestedUrl = it
                                    onUrlChanged(it)
                                }
                                view?.title?.let { t ->
                                    if (t.isNotBlank() && !t.startsWith("http")) {
                                        onTitleChanged(t)
                                    }
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    android.util.Log.e("CentralAutoLogin", "-> [onReceivedError Main Frame]: ${error?.description} code=${error?.errorCode} for ${request?.url}")
                                    isError = true
                                    isLoading = false
                                } else {
                                    android.util.Log.w("CentralAutoLogin", "-> [onReceivedError Subresource]: ${error?.description} code=${error?.errorCode} for ${request?.url}")
                                }
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: android.webkit.WebResourceResponse?
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                android.util.Log.w("CentralAutoLogin", "-> [onReceivedHttpError]: status=${errorResponse?.statusCode} reason=${errorResponse?.reasonPhrase} for ${request?.url}")
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: android.webkit.SslErrorHandler?,
                                error: android.net.http.SslError?
                            ) {
                                android.util.Log.e("CentralAutoLogin", "-> [onReceivedSslError]: $error")
                                super.onReceivedSslError(view, handler, error)
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = newProgress / 100f
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                title?.let { t ->
                                    if (t.isNotBlank() && !t.startsWith("http")) {
                                        onTitleChanged(t)
                                    }
                                }
                            }

                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("CentralConsole", "[${consoleMessage?.messageLevel()}] ${consoleMessage?.message()} -- Line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        loadUrl(url)
                    }
                },
                update = { wv ->
                    // Updating if needed
                }
            )

            AnimatedVisibility(
                visible = isLoading && !isError,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

class WebAppInterface(private val context: Context, private val fcmToken: String) {
    @JavascriptInterface
    fun getFcmToken(): String {
        return fcmToken
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
