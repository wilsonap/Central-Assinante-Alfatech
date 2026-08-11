package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
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
import com.example.service.FcmTokenStore
import com.example.ui.WhatsAppSupport
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import android.util.Log

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CentralWebView(
    url: String,
    modifier: Modifier = Modifier,
    fcmToken: String = "",
    /** When false, WebView stays alive (session/cookies) but is GONE so it cannot cover the native Home. */
    isVisible: Boolean = true,
    onWebViewCreated: (WebView) -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onUrlChanged: (String) -> Unit = {},
    onWhatsAppConfigFound: (number: String, message: String, fullUrl: String, source: String) -> Unit = { _, _, _, _ -> },
    onClientProfileFound: (fullName: String, code: String, contract: String) -> Unit = { _, _, _ -> },
    /** Chamado após transição autenticada + ciclo de refreshCentralCustomerData (ou retries esgotados). */
    onPostLoginReady: (trigger: String) -> Unit = {}
) {
    // fcmToken Compose param retained for call-site compatibility; never frozen into the bridge.
    @Suppress("UNUSED_PARAMETER")
    val unusedFcmTokenParam = fcmToken

    val context = LocalContext.current
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastRequestedUrl by remember { mutableStateOf("") }
    val bridgeController = remember { FcmBridgeController() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val dataRefreshGeneration = remember { AtomicInteger(0) }
    val recoveringFromError = remember { AtomicBoolean(false) }

    fun scheduleCustomerDataRefresh(
        webView: WebView,
        trigger: String,
        onCycleFinished: (() -> Unit)? = null
    ) {
        scheduleCentralCustomerDataRefresh(
            webView = webView,
            trigger = trigger,
            handler = mainHandler,
            generation = dataRefreshGeneration,
            onWhatsAppConfigFound = onWhatsAppConfigFound,
            onClientProfileFound = onClientProfileFound,
            onCycleFinished = onCycleFinished
        )
    }

    LaunchedEffect(bridgeController) {
        bridgeController.setOnSessionAuthenticated { webView, trigger ->
            val authType = if (trigger == "login_authenticated") "manual" else "automatic"
            Log.i("CENTRAL_POST_LOGIN", "auth_transition=unauthenticated_to_authenticated")
            Log.i("CENTRAL_POST_LOGIN", "auth_type=$authType")
            Log.i("CENTRAL_POST_LOGIN", "data_refresh_started=true")
            scheduleCustomerDataRefresh(webView, trigger) {
                Log.i("CENTRAL_POST_LOGIN", "data_refresh_finished=true")
                onPostLoginReady(trigger)
            }
        }
    }

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

    // Force GONE/VISIBLE on the same WebView instance (update{} alone is not always enough).
    LaunchedEffect(isVisible, webViewRef) {
        val wv = webViewRef ?: return@LaunchedEffect
        val target = if (isVisible) View.VISIBLE else View.GONE
        wv.visibility = target
        if (!isVisible) {
            wv.clearFocus()
        }
        android.util.Log.i(
            "CENTRAL_NAV",
            "WebView visibility=${if (isVisible) "VISIBLE" else "GONE"}"
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isError && isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        bridgeController.bindWebView(this)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            // databaseEnabled (WebSQL) removido: Central usa localStorage/sessionStorage via DOM Storage.
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
                            userAgentString =
                                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        }

                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocus()

                        val webViewInstance = this
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setAcceptThirdPartyCookies(webViewInstance, true)
                        }

                        addJavascriptInterface(
                            WebAppInterface(ctx, bridgeController),
                            "AndroidBridge"
                        )

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val reqUrl = request?.url?.toString() ?: return false
                                val isWhatsApp = reqUrl.startsWith("whatsapp://") ||
                                    reqUrl.contains("wa.me/") ||
                                    reqUrl.contains("api.whatsapp.com/send")
                                if (isWhatsApp ||
                                    reqUrl.startsWith("tel:") || reqUrl.startsWith("mailto:")
                                ) {
                                    if (isWhatsApp) {
                                        WhatsAppSupport.parseFromHref(reqUrl)?.let { cfg ->
                                            onWhatsAppConfigFound(
                                                cfg.number,
                                                cfg.message,
                                                cfg.fullUrl,
                                                "central_navigation"
                                            )
                                        }
                                    }
                                    try {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl)))
                                    } catch (_: Exception) {
                                        Toast.makeText(
                                            ctx,
                                            "Aplicativo não encontrado para abrir o link",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return true
                                }
                                if (reqUrl.endsWith(".pdf") || reqUrl.contains("download_boleto") ||
                                    reqUrl.contains("gerar_boleto")
                                ) {
                                    try {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl)))
                                        return true
                                    } catch (_: Exception) {
                                        // fall through
                                    }
                                }
                                return false
                            }

                            override fun doUpdateVisitedHistory(
                                view: WebView?,
                                url: String?,
                                isReload: Boolean
                            ) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                url?.let {
                                    lastRequestedUrl = it
                                    onUrlChanged(it)
                                    bridgeController.onUrlOrPageChanged(it)
                                }
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                // Full navigation may leave auth pages — re-evaluate bridge eligibility.
                                bridgeController.onNavigationStarted(url)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                isError = false
                                CookieManager.getInstance().flush()

                                url?.let {
                                    lastRequestedUrl = it
                                    onUrlChanged(it)
                                    bridgeController.onUrlOrPageChanged(it)
                                }
                                view?.title?.let { t ->
                                    if (t.isNotBlank() && !t.startsWith("http")) {
                                        onTitleChanged(t)
                                    }
                                }
                                // Captura WhatsApp/perfil — também reagendada após login autenticado.
                                if (view != null && !FcmBridgeController.isAuthBlockedUrl(url)) {
                                    val trigger = if (recoveringFromError.getAndSet(false)) {
                                        "network_reload"
                                    } else {
                                        "page_finished"
                                    }
                                    scheduleCustomerDataRefresh(view, trigger)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    recoveringFromError.set(true)
                                    isError = true
                                    isLoading = false
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = newProgress / 100f
                                if (newProgress >= 100) isLoading = false
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
                                val msg = consoleMessage?.message().orEmpty()
                                if (msg.contains("updateFirebaseToken", ignoreCase = true) ||
                                    msg.contains("firebase_token", ignoreCase = true)
                                ) {
                                    android.util.Log.d(
                                        "FCM_TOKEN_REGISTERED",
                                        "console: $msg -- line ${consoleMessage?.lineNumber()}"
                                    )
                                }
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        loadUrl(url)
                    }
                },
                update = { webView ->
                    // Hide native surface while Home is shown — prevents WebView from painting over Compose.
                    webView.visibility = if (isVisible) View.VISIBLE else View.GONE
                    if (!isVisible) {
                        webView.clearFocus()
                    }
                }
            )

            AnimatedVisibility(
                visible = isLoading && !isError && isVisible,
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

private const val CENTRAL_DATA_REFRESH = "CENTRAL_DATA_REFRESH"

/** Delays absolutos a partir do trigger: imediato, +500ms, +1s, +2s. */
private val CENTRAL_DATA_RETRY_DELAYS_MS = longArrayOf(0L, 500L, 1000L, 2000L)

/**
 * Reexecuta as capturas já existentes (WhatsApp DOM + perfil storage).
 * Não duplica o JavaScript de extração.
 */
private fun refreshCentralCustomerData(
    webView: WebView,
    trigger: String,
    attempt: Int,
    onWhatsAppConfigFound: (number: String, message: String, fullUrl: String, source: String) -> Unit,
    onClientProfileFound: (fullName: String, code: String, contract: String) -> Unit,
    onAttemptResult: (whatsappFound: Boolean, nameFound: Boolean, codeFound: Boolean) -> Unit
) {
    Log.i(CENTRAL_DATA_REFRESH, "trigger=$trigger attempt=$attempt")
    var whatsappFound = false
    var nameFound = false
    var codeFound = false
    val pending = AtomicInteger(2)

    fun finishOne() {
        if (pending.decrementAndGet() == 0) {
            Log.i(
                CENTRAL_DATA_REFRESH,
                "trigger=$trigger attempt=$attempt whatsappFound=$whatsappFound nameFound=$nameFound codeFound=$codeFound"
            )
            onAttemptResult(whatsappFound, nameFound, codeFound)
        }
    }

    extractWhatsAppFromDom(webView, onFound = { number, message, fullUrl, source ->
        whatsappFound = true
        onWhatsAppConfigFound(number, message, fullUrl, source)
    }, onFinished = { finishOne() })

    extractClientProfileFromStorage(webView, onFound = { fullName, code, contract ->
        if (fullName.isNotBlank()) nameFound = true
        if (code.isNotBlank()) codeFound = true
        onClientProfileFound(fullName, code, contract)
    }, onFinished = { finishOne() })
}

/**
 * Até 4 tentativas controladas; cancela agendas anteriores via [generation].
 * Para quando WhatsApp e (nome ou código) forem encontrados, ou ao esgotar as tentativas.
 */
private fun scheduleCentralCustomerDataRefresh(
    webView: WebView,
    trigger: String,
    handler: Handler,
    generation: AtomicInteger,
    onWhatsAppConfigFound: (number: String, message: String, fullUrl: String, source: String) -> Unit,
    onClientProfileFound: (fullName: String, code: String, contract: String) -> Unit,
    onCycleFinished: (() -> Unit)? = null
) {
    val gen = generation.incrementAndGet()
    val finished = AtomicBoolean(false)

    fun completeOnce() {
        if (finished.compareAndSet(false, true)) {
            onCycleFinished?.invoke()
        }
    }

    CENTRAL_DATA_RETRY_DELAYS_MS.forEachIndexed { index, delayMs ->
        handler.postDelayed({
            if (generation.get() != gen || finished.get()) return@postDelayed
            if (FcmBridgeController.isAuthBlockedUrl(webView.url)) {
                // Logout / tela de login no meio do ciclo: não navegar à Home.
                finished.set(true)
                return@postDelayed
            }
            refreshCentralCustomerData(
                webView = webView,
                trigger = trigger,
                attempt = index + 1,
                onWhatsAppConfigFound = onWhatsAppConfigFound,
                onClientProfileFound = onClientProfileFound,
                onAttemptResult = { wa, name, code ->
                    if (generation.get() != gen) return@refreshCentralCustomerData
                    if (wa && (name || code)) {
                        completeOnce()
                    } else if (index == CENTRAL_DATA_RETRY_DELAYS_MS.lastIndex) {
                        completeOnce()
                    }
                }
            )
        }, delayMs)
    }
}

private fun extractWhatsAppFromDom(
    webView: WebView,
    onFound: (number: String, message: String, fullUrl: String, source: String) -> Unit,
    onFinished: () -> Unit = {}
) {
    val script = """
        (function() {
          try {
            function pickHref() {
              var preferred = document.querySelector(
                'a[href*="api.whatsapp.com/send?phone="],a[href*="api.whatsapp.com/send/?phone="]'
              );
              if (preferred && preferred.href) return preferred.href;
              var fallback = document.querySelector('a[href*="wa.me/"]');
              if (fallback && fallback.href) return fallback.href;
              var links = document.getElementsByTagName('a');
              for (var i = 0; i < links.length; i++) {
                var h = links[i].href || '';
                if (h.indexOf('api.whatsapp.com/send') >= 0 || h.indexOf('wa.me/') >= 0) {
                  return h;
                }
              }
              return '';
            }
            return pickHref();
          } catch (e) {
            return '';
          }
        })();
    """.trimIndent()

    webView.evaluateJavascript(script) { raw ->
        try {
            val href = raw
                ?.trim()
                ?.removePrefix("\"")
                ?.removeSuffix("\"")
                ?.replace("\\u003d", "=")
                ?.replace("\\u0026", "&")
                ?.replace("\\/", "/")
                ?.replace("\\\"", "\"")
                ?.takeIf { it.isNotBlank() && it != "null" && it != "undefined" }

            if (href != null) {
                WhatsAppSupport.parseFromHref(href)?.let { cfg ->
                    onFound(cfg.number, cfg.message, cfg.fullUrl, "central_dom")
                }
            }
        } finally {
            onFinished()
        }
    }
}

private fun extractClientProfileFromStorage(
    webView: WebView,
    onFound: (fullName: String, code: String, contract: String) -> Unit,
    onFinished: () -> Unit = {}
) {
    // Meus Dados / storage IXC: nome (razao), codigo e contrato quando existirem.
    val script = """
        (function() {
          try {
            function parseStore(key) {
              try {
                var raw = localStorage.getItem(key) || sessionStorage.getItem(key);
                if (!raw) return null;
                return JSON.parse(raw);
              } catch (e) { return null; }
            }
            function pick() {
              for (var i = 0; i < arguments.length; i++) {
                var v = arguments[i];
                if (v !== undefined && v !== null) {
                  var s = String(v).trim();
                  if (s && s !== 'null' && s !== 'undefined') return s;
                }
              }
              return '';
            }
            var dados = parseStore('dados') || {};
            var cliente = parseStore('cliente') || {};
            var contratoObj = parseStore('contrato') || parseStore('contratos') || {};
            var nome = pick(
              dados.razao, dados.nome_completo, dados.nome, dados.fantasia,
              cliente.razao, cliente.nome_completo, cliente.nome
            );
            var codigo = pick(
              dados.codigo_cliente, dados.codigo, dados.id_cliente, dados.id,
              cliente.codigo_cliente, cliente.codigo, cliente.id_cliente, cliente.id
            );
            var contrato = pick(
              dados.contrato, dados.id_contrato, dados.codigo_contrato,
              cliente.contrato, cliente.id_contrato,
              contratoObj.contrato, contratoObj.id, contratoObj.codigo, contratoObj.id_contrato
            );
            if (Array.isArray(contratoObj) && contratoObj.length > 0) {
              var c0 = contratoObj[0] || {};
              if (!contrato) contrato = pick(c0.contrato, c0.id, c0.codigo, c0.id_contrato);
            }
            return JSON.stringify({ nome: nome, codigo: codigo, contrato: contrato });
          } catch (e) {
            return JSON.stringify({ nome: '', codigo: '', contrato: '' });
          }
        })();
    """.trimIndent()

    webView.evaluateJavascript(script) { raw ->
        try {
            val jsonText = raw
                ?.trim()
                ?.removePrefix("\"")
                ?.removeSuffix("\"")
                ?.replace("\\\"", "\"")
                ?.replace("\\\\", "\\")
                ?.takeIf { it.isNotBlank() && it != "null" && it != "undefined" }
                ?: return@evaluateJavascript
            try {
                val obj = org.json.JSONObject(jsonText)
                val nome = obj.optString("nome", "").trim()
                val codigo = obj.optString("codigo", "").trim()
                val contrato = obj.optString("contrato", "").trim()
                if (nome.isBlank() && codigo.isBlank() && contrato.isBlank()) return@evaluateJavascript
                onFound(nome, codigo, contrato)
            } catch (_: Exception) {
                // ignore malformed extract
            }
        } finally {
            onFinished()
        }
    }
}

/**
 * Controla habilitação pós-auth do shim ReactNativeWebView.postMessage
 * e respostas ao pedido firebase_token da Central.
 */
class FcmBridgeController {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val webViewRef = AtomicReference<WebView?>(null)
    private val bridgeEnabled = AtomicBoolean(false)
    private val tokenRequestSeen = AtomicBoolean(false)
    private val updateFirebaseFallbackUsed = AtomicBoolean(false)
    private val fallbackScheduled = AtomicBoolean(false)
    /** Sessão IXC já observada como autenticada (cookie/storage sessao). */
    private val hadAuthenticatedSession = AtomicBoolean(false)
    /** Passou por tela de login/cadastro — próximo ok ⇒ login_authenticated. */
    private val sawAuthBlockedPath = AtomicBoolean(false)
    private var onSessionAuthenticated: ((WebView, String) -> Unit)? = null

    fun bindWebView(webView: WebView) {
        webViewRef.set(webView)
    }

    fun setOnSessionAuthenticated(listener: (WebView, String) -> Unit) {
        onSessionAuthenticated = listener
    }

    fun onNavigationStarted(url: String?) {
        if (isAuthBlockedUrl(url)) {
            sawAuthBlockedPath.set(true)
            hadAuthenticatedSession.set(false)
            // Never keep bridge visible on login/cadastro/senha screens.
            if (bridgeEnabled.getAndSet(false)) {
                removeReactNativeShim()
                android.util.Log.i("FCM_AUTH_STATE", "bridge removed — blocked path url=$url")
            }
        }
    }

    fun onUrlOrPageChanged(url: String?) {
        val wv = webViewRef.get() ?: return
        if (isAuthBlockedUrl(url)) {
            android.util.Log.i("FCM_AUTH_STATE", "blocked_path — bridge not enabled url=$url")
            sawAuthBlockedPath.set(true)
            hadAuthenticatedSession.set(false)
            if (bridgeEnabled.getAndSet(false)) {
                removeReactNativeShim()
            }
            return
        }
        evaluateAuthAndMaybeEnable(wv, url)
    }

    fun onFirebaseTokenRequest(promiseId: String) {
        tokenRequestSeen.set(true)
        android.util.Log.i(
            "FCM_TOKEN_REQUEST",
            "type=firebase_token promiseID=$promiseId"
        )
        respondWithCurrentToken(promiseId)
    }

    private fun evaluateAuthAndMaybeEnable(webView: WebView, url: String?) {
        if (isAuthBlockedUrl(url) || isAuthBlockedUrl(webView.url)) {
            android.util.Log.i("FCM_AUTH_STATE", "skip enable — still on auth-sensitive path")
            sawAuthBlockedPath.set(true)
            hadAuthenticatedSession.set(false)
            return
        }

        val authProbe = """
            (function() {
              try {
                var path = (window.location.pathname || '').toLowerCase();
                var href = (window.location.href || '').toLowerCase();
                var blocked =
                  path.indexOf('/login') >= 0 ||
                  href.indexOf('/login') >= 0 ||
                  path.indexOf('cadastro_login') >= 0 ||
                  path.indexOf('/cadastro') >= 0 ||
                  path.indexOf('trocarsenha') >= 0 ||
                  path.indexOf('trocar_senha') >= 0 ||
                  path.indexOf('trocar-senha') >= 0 ||
                  path.indexOf('recuper') >= 0;
                if (blocked) {
                  return JSON.stringify({ ok: false, reason: 'blocked_path' });
                }
                var cookieHas = false;
                try {
                  cookieHas = (document.cookie || '').split(';').some(function(c) {
                    var p = c.trim();
                    return p.indexOf('sessao=') === 0 && p.length > 'sessao='.length;
                  });
                } catch (e0) {}
                var ls = false, ss = false;
                try { ls = !!localStorage.getItem('sessao'); } catch (e1) {}
                try { ss = !!sessionStorage.getItem('sessao'); } catch (e2) {}
                var hasSession = cookieHas || ls || ss;
                return JSON.stringify({
                  ok: hasSession,
                  reason: hasSession ? 'session_evidence' : 'no_session',
                  hasCookie: cookieHas,
                  hasLs: ls,
                  hasSs: ss
                });
              } catch (e) {
                return JSON.stringify({ ok: false, reason: 'error' });
              }
            })();
        """.trimIndent()

        mainHandler.post {
            webView.evaluateJavascript(authProbe) { raw ->
                val json = unwrapJsString(raw)
                try {
                    val obj = JSONObject(json ?: "{}")
                    val ok = obj.optBoolean("ok", false)
                    val reason = obj.optString("reason", "?")
                    android.util.Log.i(
                        "FCM_AUTH_STATE",
                        "url=$url ok=$ok reason=$reason hasCookie=${obj.optBoolean("hasCookie")} hasLs=${obj.optBoolean("hasLs")} hasSs=${obj.optBoolean("hasSs")}"
                    )
                    if (reason == "blocked_path") {
                        sawAuthBlockedPath.set(true)
                        hadAuthenticatedSession.set(false)
                        return@evaluateJavascript
                    }
                    if (ok) {
                        val becameAuthenticated = !hadAuthenticatedSession.getAndSet(true)
                        if (becameAuthenticated) {
                            val trigger = if (sawAuthBlockedPath.getAndSet(false)) {
                                "login_authenticated"
                            } else {
                                "session_restored"
                            }
                            Log.i(CENTRAL_DATA_REFRESH, "trigger=$trigger")
                            onSessionAuthenticated?.invoke(webView, trigger)
                        }
                        if (!bridgeEnabled.get()) {
                            enableReactNativePostMessageShim(webView)
                        }
                    }
                    // !ok sem blocked_path: falha temporária — não zera dados nem hadAuthenticatedSession.
                } catch (e: Exception) {
                    android.util.Log.e("FCM_AUTH_STATE", "parse auth probe failed: $raw", e)
                }
            }
        }
    }

    private fun enableReactNativePostMessageShim(webView: WebView) {
        if (bridgeEnabled.get()) return
        // Inject ONLY ReactNativeWebView.postMessage — no message listener recreation.
        val script = """
            (function() {
              try {
                if (window.ReactNativeWebView && typeof window.ReactNativeWebView.postMessage === 'function') {
                  return 'already';
                }
                window.ReactNativeWebView = {
                  postMessage: function(message) {
                    try {
                      if (window.AndroidBridge && typeof window.AndroidBridge.onReactNativeMessage === 'function') {
                        window.AndroidBridge.onReactNativeMessage(String(message));
                      }
                    } catch (e) {}
                  }
                };
                return 'enabled';
              } catch (e) {
                return 'error:' + String(e);
              }
            })();
        """.trimIndent()

        mainHandler.post {
            if (isAuthBlockedUrl(webView.url)) {
                android.util.Log.i("FCM_AUTH_STATE", "abort enable — landed on blocked path")
                return@post
            }
            webView.evaluateJavascript(script) { result ->
                val status = unwrapJsString(result) ?: result
                if (status == "enabled" || status == "already") {
                    bridgeEnabled.set(true)
                    android.util.Log.i("FCM_BRIDGE_ENABLED", "status=$status url=${webView.url}")
                    scheduleUpdateFirebaseTokenFallback(webView)
                } else {
                    android.util.Log.e("FCM_BRIDGE_ENABLED", "failed status=$status")
                }
            }
        }
    }

    private fun removeReactNativeShim() {
        val webView = webViewRef.get() ?: return
        mainHandler.post {
            webView.evaluateJavascript(
                """
                (function() {
                  try { delete window.ReactNativeWebView; } catch (e) {
                    try { window.ReactNativeWebView = undefined; } catch (e2) {}
                  }
                  return 'removed';
                })();
                """.trimIndent(),
                null
            )
        }
    }

    private fun scheduleUpdateFirebaseTokenFallback(webView: WebView) {
        if (!fallbackScheduled.compareAndSet(false, true)) return
        // Wait for natural iniciaFirebase → updateFirebaseToken → firebase_token.
        mainHandler.postDelayed({
            if (tokenRequestSeen.get() || updateFirebaseFallbackUsed.get()) return@postDelayed
            if (!bridgeEnabled.get()) return@postDelayed
            if (isAuthBlockedUrl(webView.url)) return@postDelayed
            verifyBridgeEnvironmentThenMaybeFallback(webView)
        }, 6_000L)
    }

    private fun verifyBridgeEnvironmentThenMaybeFallback(webView: WebView) {
        val verifyScript = """
            (function() {
              try {
                var hasPromises =
                  (typeof promisesWebView !== 'undefined') ||
                  (typeof window.promisesWebView !== 'undefined');
                var hasHotsite = typeof HotsiteWeb === 'function';
                var hasUpdate = false;
                if (hasHotsite && HotsiteWeb.prototype) {
                  hasUpdate = typeof HotsiteWeb.prototype.updateFirebaseToken === 'function';
                }
                var hasRn = !!(window.ReactNativeWebView && window.ReactNativeWebView.postMessage);
                return JSON.stringify({
                  hasPromises: hasPromises,
                  hasHotsite: hasHotsite,
                  hasUpdate: hasUpdate,
                  hasRn: hasRn
                });
              } catch (e) {
                return JSON.stringify({ error: String(e) });
              }
            })();
        """.trimIndent()

        webView.evaluateJavascript(verifyScript) { raw ->
            val json = unwrapJsString(raw)
            android.util.Log.i("FCM_AUTH_STATE", "pre-fallback verify=$json")
            try {
                val obj = JSONObject(json ?: "{}")
                if (!obj.optBoolean("hasUpdate", false) || !obj.optBoolean("hasRn", false)) {
                    android.util.Log.w(
                        "FCM_AUTH_STATE",
                        "fallback skipped — updateFirebaseToken/ReactNativeWebView unavailable"
                    )
                    return@evaluateJavascript
                }
                // Probe MessageEvent / promisesWebView without registering a second listener.
                val probeId = "_fcm_probe"
                val probePayload = JSONObject()
                    .put("promiseID", probeId)
                    .put("token", "probe")
                    .toString()
                val probeQuoted = JSONObject.quote(probePayload)
                val probeScript = """
                    (function() {
                      try {
                        var payload = $probeQuoted;
                        var listenerWorks = false;
                        var promisesExists =
                          (typeof promisesWebView !== 'undefined') ||
                          (typeof window.promisesWebView !== 'undefined');
                        try {
                          window.dispatchEvent(new MessageEvent('message', { data: payload }));
                          listenerWorks = true;
                        } catch (e1) {
                          listenerWorks = false;
                        }
                        return JSON.stringify({
                          promisesExists: promisesExists,
                          messageEventOk: listenerWorks
                        });
                      } catch (e) {
                        return JSON.stringify({ error: String(e) });
                      }
                    })();
                """.trimIndent()

                webView.evaluateJavascript(probeScript) { probeRaw ->
                    android.util.Log.i(
                        "FCM_AUTH_STATE",
                        "message_event_probe=${unwrapJsString(probeRaw)}"
                    )
                    if (tokenRequestSeen.get() || updateFirebaseFallbackUsed.get()) return@evaluateJavascript
                    if (!updateFirebaseFallbackUsed.compareAndSet(false, true)) return@evaluateJavascript
                    android.util.Log.i(
                        "FCM_AUTH_STATE",
                        "no natural firebase_token — calling updateFirebaseToken() once"
                    )
                    webView.evaluateJavascript(
                        """
                        (function() {
                          try {
                            if (typeof HotsiteWeb === 'function') {
                              new HotsiteWeb().updateFirebaseToken();
                              return 'called';
                            }
                            return 'missing';
                          } catch (e) {
                            return 'error:' + String(e);
                          }
                        })();
                        """.trimIndent()
                    ) { callResult ->
                        android.util.Log.i(
                            "FCM_TOKEN_REGISTERED",
                            "updateFirebaseToken fallback result=${unwrapJsString(callResult)}"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FCM_AUTH_STATE", "fallback verify failed", e)
            }
        }
    }

    private fun respondWithCurrentToken(promiseId: String) {
        val webView = webViewRef.get() ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful && !task.result.isNullOrBlank()) {
                task.result
            } else {
                FcmTokenStore.current(webView.context)
            }
            if (token.isNullOrBlank()) {
                android.util.Log.e("FCM_TOKEN_READY", "token unavailable promiseID=$promiseId")
                return@addOnCompleteListener
            }
            FcmTokenStore.update(webView.context, token)
            android.util.Log.i(
                "FCM_TOKEN_READY",
                "promiseID=$promiseId token=${FcmTokenStore.mask(token)}"
            )
            dispatchTokenResponse(webView, promiseId, token)
        }
    }

    private fun dispatchTokenResponse(webView: WebView, promiseId: String, token: String) {
        val payload = JSONObject()
            .put("promiseID", promiseId)
            .put("token", token)
            .toString()
        val payloadLiteral = JSONObject.quote(payload)

        // rotas.js: addEventListener('message', ...) on global/window.
        // After MessageEvent, complete promisesWebView[id] if still pending
        // (late shim: page listener may be absent because ReactNativeWebView
        // was undefined when rotas.js loaded). Promise resolve is idempotent.
        val script = """
            (function() {
              try {
                var payload = $payloadLiteral;
                var data = JSON.parse(payload);
                var id = data.promiseID;
                var dispatched = false;
                try {
                  window.dispatchEvent(new MessageEvent('message', { data: payload }));
                  dispatched = true;
                } catch (e1) {}
                try {
                  if (typeof document !== 'undefined' && document.dispatchEvent) {
                    document.dispatchEvent(new MessageEvent('message', { data: payload }));
                  }
                } catch (e2) {}
                try {
                  var bag = (typeof promisesWebView !== 'undefined')
                    ? promisesWebView
                    : (typeof window.promisesWebView !== 'undefined' ? window.promisesWebView : null);
                  if (bag && bag[id] && typeof bag[id].resolve === 'function') {
                    bag[id].resolve({ data: payload });
                  }
                } catch (e3) {}
                return JSON.stringify({ ok: true, dispatched: dispatched, promiseID: id });
              } catch (e) {
                return JSON.stringify({ ok: false, error: String(e) });
              }
            })();
        """.trimIndent()

        mainHandler.post {
            webView.evaluateJavascript(script) { result ->
                android.util.Log.i(
                    "FCM_TOKEN_RESPONSE",
                    "promiseID=$promiseId token=${FcmTokenStore.mask(token)} result=${unwrapJsString(result)}"
                )
                android.util.Log.i(
                    "FCM_TOKEN_REGISTERED",
                    "response delivered to page promiseID=$promiseId (Central should POST updateFirebaseToken)"
                )
            }
        }
    }

    companion object {
        fun isAuthBlockedUrl(url: String?): Boolean {
            if (url.isNullOrBlank()) return false
            val u = url.lowercase()
            return u.contains("/login") ||
                u.contains("cadastro_login") ||
                u.contains("/cadastro") ||
                u.contains("trocarsenha") ||
                u.contains("trocar_senha") ||
                u.contains("trocar-senha") ||
                u.contains("recuperar") ||
                u.contains("recupera_senha")
        }

        fun unwrapJsString(raw: String?): String? {
            if (raw == null || raw == "null") return null
            return try {
                // evaluateJavascript returns a JSON-encoded string
                org.json.JSONTokener(raw).nextValue()?.toString() ?: raw.trim('"')
            } catch (_: Exception) {
                raw.trim().trim('"')
            }
        }
    }
}

class WebAppInterface(
    private val context: Context,
    private val bridgeController: FcmBridgeController
) {
    @JavascriptInterface
    fun getFcmToken(): String {
        // Live store value only — never a constructor-frozen token.
        return FcmTokenStore.current(context)
    }

    @JavascriptInterface
    fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Recebe mensagens no formato da Central:
     * {"type":"firebase_token","promiseID":"_xxxx", ...}
     */
    @JavascriptInterface
    fun onReactNativeMessage(message: String) {
        try {
            val obj = JSONObject(message)
            val type = obj.optString("type", "")
            val promiseId = obj.optString("promiseID", "")
            if (type == "firebase_token" && promiseId.isNotBlank()) {
                bridgeController.onFirebaseTokenRequest(promiseId)
            } else {
                android.util.Log.d(
                    "FCM_TOKEN_REQUEST",
                    "ignored type=$type promiseID=$promiseId"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("FCM_TOKEN_REQUEST", "invalid message from page", e)
        }
    }
}
