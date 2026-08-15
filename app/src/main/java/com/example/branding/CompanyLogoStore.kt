package com.example.branding

import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Logo da Central: baixa e persiste em filesDir/company_logo/ para uso offline.
 * O cabeçalho Compose deve usar sempre [localLogoPath], não a URL remota.
 */
object CompanyLogoStore {

    private const val PREFS = "company_logo_prefs"
    private const val KEY_URL = "company_logo_url"
    private const val KEY_LOCAL_PATH = "company_logo_local_path"
    private const val KEY_UPDATED_AT = "company_logo_updated_at"
    private const val TAG = "COMPANY_LOGO"
    private const val DIR_NAME = "company_logo"
    private const val MAX_BYTES = 2L * 1024L * 1024L // 2 MB

    private val downloadMutex = Mutex()
    private val downloading = AtomicBoolean(false)

    /** Caminho absoluto do arquivo local (ou null → fallback UI). */
    private val _localLogoPath = MutableStateFlow<String?>(null)
    val localLogoPath: StateFlow<String?> = _localLogoPath.asStateFlow()

    /** @deprecated Prefer [localLogoPath]. Mantido só se algum call-site ainda observar. */
    val logoUrl: StateFlow<String?> get() = localLogoPath

    fun hydrate(context: Context) {
        val app = context.applicationContext
        val p = prefs(app)
        val path = p.getString(KEY_LOCAL_PATH, null)?.trim().orEmpty()
        val file = if (path.isNotEmpty()) File(path) else null
        if (file != null && file.isFile && file.length() > 0L) {
            _localLogoPath.value = file.absolutePath
            Log.i(TAG, "source=local_cache")
            Log.i(TAG, "hydrate=true")
        } else {
            _localLogoPath.value = null
            Log.i(TAG, "fallback=true")
            Log.i(TAG, "hydrate=false")
        }
    }

    /**
     * Chamado quando o DOM entrega uma URL.
     * Baixa (ou decodifica data:) e grava em disco se a URL for nova ou o arquivo local sumiu.
     */
    suspend fun updateFromCentral(context: Context, rawUrl: String?): Boolean =
        withContext(Dispatchers.IO) {
            val app = context.applicationContext
            val sanitized = sanitize(rawUrl)
            if (sanitized == null) {
                Log.i(TAG, "found=false")
                return@withContext false
            }
            Log.i(TAG, "found=true")
            Log.i(TAG, "urlHost=${hostForLog(sanitized)}")

            val p = prefs(app)
            val oldUrl = p.getString(KEY_URL, null)
            val localPath = p.getString(KEY_LOCAL_PATH, null)
            val localFile = localPath?.let { File(it) }
            val localOk = localFile != null && localFile.isFile && localFile.length() > 0L

            if (oldUrl == sanitized && localOk) {
                _localLogoPath.value = localFile!!.absolutePath
                Log.i(TAG, "source=local_cache")
                return@withContext false
            }

            downloadMutex.withLock {
                if (!downloading.compareAndSet(false, true)) {
                    return@withLock false
                }
                try {
                    Log.i(TAG, "downloadStarted=true")
                    val saved = persistLogoBytes(app, sanitized) ?: run {
                        Log.i(TAG, "savedLocal=false")
                        if (localOk) {
                            _localLogoPath.value = localFile!!.absolutePath
                            Log.i(TAG, "source=local_cache")
                        } else {
                            Log.i(TAG, "fallback=true")
                        }
                        return@withLock false
                    }
                    p.edit()
                        .putString(KEY_URL, sanitized.take(2048)) // evita prefs gigantes com data:
                        .putString(KEY_LOCAL_PATH, saved.absolutePath)
                        .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                        .apply()
                    _localLogoPath.value = saved.absolutePath
                    Log.i(TAG, "savedLocal=true")
                    Log.i(TAG, "source=remote")
                    true
                } finally {
                    downloading.set(false)
                }
            }
        }

    fun sanitize(raw: String?): String? {
        val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val normalized = when {
            trimmed.startsWith("data:application/image;base64,", ignoreCase = true) ->
                "data:image/png;base64," + trimmed.substringAfter(',')
            trimmed.startsWith("data:image/", ignoreCase = true) -> trimmed
            else -> trimmed
        }

        if (normalized.startsWith("data:image/", ignoreCase = true)) {
            if (!normalized.contains(";base64,", ignoreCase = true)) return null
            return normalized
        }

        val uri = try {
            URI(normalized)
        } catch (_: Exception) {
            return null
        }
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        val host = uri.host?.lowercase() ?: return null
        if (!isAllowedHost(host)) return null
        return normalized
    }

    private fun persistLogoBytes(context: Context, sanitizedUrl: String): File? {
        val bytes: ByteArray
        val ext: String
        when {
            sanitizedUrl.startsWith("data:image/", ignoreCase = true) -> {
                val meta = sanitizedUrl.substringBefore(',')
                val b64 = sanitizedUrl.substringAfter(',', missingDelimiterValue = "")
                if (b64.isBlank()) return null
                bytes = try {
                    Base64.decode(b64, Base64.DEFAULT)
                } catch (_: Exception) {
                    return null
                }
                if (bytes.isEmpty() || bytes.size > MAX_BYTES) return null
                if (!looksLikeImage(bytes)) return null
                ext = when {
                    meta.contains("jpeg", ignoreCase = true) ||
                        meta.contains("jpg", ignoreCase = true) -> "jpg"
                    meta.contains("webp", ignoreCase = true) -> "webp"
                    meta.contains("gif", ignoreCase = true) -> "gif"
                    else -> "png"
                }
            }
            else -> {
                val downloaded = downloadHttp(sanitizedUrl) ?: return null
                bytes = downloaded.first
                ext = downloaded.second
            }
        }

        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists() && !dir.mkdirs()) return null
        // Remove cópias antigas no diretório (somente company_logo.*).
        dir.listFiles()?.forEach { f ->
            if (f.isFile && f.name.startsWith("company_logo")) {
                runCatching { f.delete() }
            }
        }
        val out = File(dir, "company_logo.$ext")
        return try {
            out.outputStream().use { it.write(bytes) }
            if (!out.isFile || out.length() == 0L) null else out
        } catch (_: Exception) {
            runCatching { out.delete() }
            null
        }
    }

    private fun downloadHttp(urlStr: String): Pair<ByteArray, String>? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "image/*")
            }
            val code = conn.responseCode
            if (code !in 200..299) return null
            val contentLength = conn.contentLengthLong
            if (contentLength > MAX_BYTES) return null
            val contentType = conn.contentType.orEmpty()
            if (contentType.isNotBlank() &&
                !contentType.startsWith("image/", ignoreCase = true) &&
                !contentType.contains("octet-stream", ignoreCase = true)
            ) {
                return null
            }
            val bytes = conn.inputStream.use { input ->
                val buf = ByteArray(8 * 1024)
                val out = java.io.ByteArrayOutputStream()
                var total = 0L
                while (true) {
                    val n = input.read(buf)
                    if (n <= 0) break
                    total += n
                    if (total > MAX_BYTES) return null
                    out.write(buf, 0, n)
                }
                out.toByteArray()
            }
            if (bytes.isEmpty() || !looksLikeImage(bytes)) return null
            val ext = extensionFor(urlStr, contentType, bytes)
            bytes to ext
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun extensionFor(url: String, contentType: String, bytes: ByteArray): String {
        val ct = contentType.lowercase()
        when {
            ct.contains("jpeg") || ct.contains("jpg") -> return "jpg"
            ct.contains("webp") -> return "webp"
            ct.contains("gif") -> return "gif"
            ct.contains("png") -> return "png"
        }
        val path = try {
            URI(url).path.orEmpty().lowercase()
        } catch (_: Exception) {
            ""
        }
        when {
            path.endsWith(".jpg") || path.endsWith(".jpeg") -> return "jpg"
            path.endsWith(".webp") -> return "webp"
            path.endsWith(".gif") -> return "gif"
            path.endsWith(".png") -> return "png"
        }
        return when {
            bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "jpg"
            bytes.size >= 12 && String(bytes, 0, 4) == "RIFF" -> "webp"
            bytes.size >= 6 && String(bytes, 0, 3) == "GIF" -> "gif"
            else -> "png"
        }
    }

    private fun looksLikeImage(bytes: ByteArray): Boolean {
        if (bytes.size < 8) return false
        // PNG
        if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte()) {
            return true
        }
        // JPEG
        if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) return true
        // GIF
        if (bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()) {
            return true
        }
        // WEBP (RIFF....WEBP)
        if (bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte()
        ) {
            return true
        }
        return false
    }

    private fun isAllowedHost(host: String): Boolean {
        return host == "sac2.alfatechtelecom.com.br" ||
            host.endsWith(".alfatechtelecom.com.br") ||
            host == "alfatechtelecom.com.br"
    }

    fun hostForLog(url: String?): String {
        if (url.isNullOrBlank()) return "-"
        if (url.startsWith("data:", ignoreCase = true)) return "data-image"
        return try {
            URI(url).host ?: "-"
        } catch (_: Exception) {
            "-"
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
