package com.example.receipt

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Compartilhamento de comprovante — fluxos separados (sem combinar wa.me + EXTRA_STREAM):
 *
 * 1) [shareFileToWhatsApp]: somente ACTION_SEND + createChooser (sem setPackage).
 * 2) [openAlfatechChat]: somente ACTION_VIEW / wa.me com supportWhatsAppNumber (sem anexo).
 */
object ReceiptShareHelper {

    private const val TAG = "RECEIPT_SHARE"
    private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

    fun normalizePhone(raw: String?): String =
        raw.orEmpty().filter { it.isDigit() }

    fun formatJid(phoneDigits: String): String {
        val clean = normalizePhone(phoneDigits)
        val formatted = when {
            clean.length == 10 || clean.length == 11 -> "55$clean"
            clean.startsWith("55") -> clean
            else -> clean
        }
        return "$formatted@s.whatsapp.net"
    }

    fun buildMessage(fullName: String): String {
        return buildString {
            append("Olá, Alfatech Telecom. Segue meu comprovante de pagamento.")
            if (fullName.isNotBlank()) {
                append("\n\nCliente: ").append(fullName.trim())
            }
            append("\n\nEnviado pelo app Central do Assinante Alfatech.")
        }
    }

    /**
     * Enviar comprovante: ACTION_SEND + EXTRA_STREAM + EXTRA_TEXT + ClipData + createChooser.
     * Sem setPackage ou com package resolvido dinamicamente. Sem wa.me.
     */
    fun shareFileToWhatsApp(
        context: Context,
        fileUri: Uri,
        mimeType: String,
        phoneDigits: String?,
        message: String
    ): Boolean {
        val digits = normalizePhone(phoneDigits)
        if (digits.isEmpty()) {
            Toast.makeText(
                context,
                "WhatsApp de atendimento ainda não disponível. Abra a Central do Assinante e tente novamente.",
                Toast.LENGTH_LONG
            ).show()
            Log.w(TAG, "share blocked — no support number")
            return false
        }

        val authority = fileProviderAuthority(context)
        val shareUri = ensureShareableContentUri(context, fileUri, mimeType, authority) ?: run {
            Toast.makeText(context, "Não foi possível preparar o comprovante.", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "share blocked — uri not shareable")
            return false
        }

        if (shareUri.scheme != "content") {
            Log.e(TAG, "scheme inválido — somente content://")
            Toast.makeText(context, "URI do comprovante inválida.", Toast.LENGTH_SHORT).show()
            return false
        }

        val isPdf = mimeType.contains("pdf", ignoreCase = true)
        val validation = validatePhysicalFile(context, shareUri, isPdf)
        if (!validation.readable || validation.byteCount <= 0L) {
            Log.e(TAG, "arquivo inválido ou inacessível no momento do share: readable=${validation.readable}, bytes=${validation.byteCount}")
            Toast.makeText(context, "Arquivo do comprovante não está acessível.", Toast.LENGTH_SHORT).show()
            return false
        }

        val exactMime = if (isPdf) "application/pdf" else "image/jpeg"
        val cleanDigits = if (digits.startsWith("55")) digits else "55$digits"
        val jid = "$cleanDigits@s.whatsapp.net"

        // Concede permissão explícita aos pacotes do WhatsApp
        for (pkg in WHATSAPP_PACKAGES) {
            try {
                context.grantUriPermission(pkg, shareUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                Log.w(TAG, "grantUriPermission fail for $pkg: ${e.message}")
            }
        }

        fun createShareIntent(packageName: String?): Intent {
            return Intent(Intent.ACTION_SEND).apply {
                type = exactMime
                putExtra(Intent.EXTRA_STREAM, shareUri)
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra("jid", jid)
                clipData = ClipData.newRawUri("comprovante", shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (packageName != null) {
                    setPackage(packageName)
                }
            }
        }

        // AUDIT LOGGING (RECEIPT_SHARE_AUDIT)
        val sourceTag = if (isPdf) "pdf" else "stamped"
        Log.i(
            "RECEIPT_SHARE_AUDIT",
            "source=$sourceTag " +
            "scheme=${shareUri.scheme} " +
            "mime=$exactMime " +
            "readable=${validation.readable} " +
            "byteCount=${validation.byteCount} " +
            "authority=${shareUri.authority} " +
            "jid=$jid"
        )

        // Tenta disparar diretamente para com.whatsapp, depois com.whatsapp.w4b (Business), e por fim via Chooser
        return try {
            context.startActivity(createShareIntent("com.whatsapp"))
            true
        } catch (e1: Exception) {
            Log.w(TAG, "Direct com.whatsapp failed: ${e1.message}, trying com.whatsapp.w4b")
            try {
                context.startActivity(createShareIntent("com.whatsapp.w4b"))
                true
            } catch (e2: Exception) {
                Log.w(TAG, "Direct com.whatsapp.w4b failed: ${e2.message}, falling back to Chooser")
                try {
                    val chooserIntent = Intent.createChooser(createShareIntent(null), "Enviar comprovante").apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        clipData = ClipData.newRawUri("comprovante", shareUri)
                    }
                    context.startActivity(chooserIntent)
                    true
                } catch (e3: Exception) {
                    Log.e(TAG, "All share attempts failed", e3)
                    Toast.makeText(context, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    private data class FileValidation(
        val readable: Boolean,
        val byteCount: Long
    )

    private fun validatePhysicalFile(context: Context, uri: Uri, isPdf: Boolean): FileValidation {
        return try {
            var size = 0L
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                size = pfd.statSize
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                val header = ByteArray(4)
                val read = input.read(header)
                if (read <= 0) return FileValidation(false, 0L)

                if (isPdf) {
                    // Check %PDF header
                    if (read >= 4 && header[0] == 0x25.toByte() && header[1] == 0x50.toByte() && header[2] == 0x44.toByte() && header[3] == 0x46.toByte()) {
                        Log.d(TAG, "PDF header verified (%PDF)")
                    }
                } else {
                    // Check JPEG header (0xFF, 0xD8)
                    if (read >= 2 && (header[0].toInt() and 0xFF) == 0xFF && (header[1].toInt() and 0xFF) == 0xD8) {
                        Log.d(TAG, "JPEG header verified (0xFFD8)")
                    }
                }

                if (size <= 0L) {
                    var total = read.toLong()
                    val buf = ByteArray(8192)
                    var r: Int
                    while (input.read(buf).also { r = it } != -1) {
                        total += r
                    }
                    size = total
                }
            }
            FileValidation(readable = size > 0L, byteCount = size)
        } catch (e: Exception) {
            Log.w(TAG, "validatePhysicalFile fail=${e.javaClass.simpleName}")
            FileValidation(readable = false, byteCount = 0L)
        }
    }

    private fun resolveSingleInstalledWhatsAppPackage(context: Context): String? {
        val pm = context.packageManager
        val installed = mutableListOf<String>()
        for (pkg in WHATSAPP_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                installed.add(pkg)
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }
        // Retorna o pacote somente se houver exatamente um instalado (ex: só WhatsApp ou só Business).
        // Se ambos estiverem instalados, retorna null para permitir que o Chooser apresente as duas opções.
        return if (installed.size == 1) installed.first() else null
    }

    /**
     * Abrir conversa Alfatech: ACTION_VIEW / wa.me apenas.
     * Sem EXTRA_STREAM — anexo manual pelo usuário.
     */
    fun openAlfatechChat(
        context: Context,
        phoneDigits: String?,
        message: String
    ): Boolean {
        val digits = normalizePhone(phoneDigits)
        if (digits.isEmpty()) {
            Toast.makeText(
                context,
                "WhatsApp de atendimento ainda não disponível. Abra a Central do Assinante e tente novamente.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val webUri = Uri.parse(
            "https://wa.me/$digits?text=${Uri.encode(message)}"
        )
        val pkg = resolveInstalledWhatsAppPackage(context)
        if (pkg != null) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, webUri).apply {
                        setPackage(pkg)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                return true
            } catch (_: Exception) {
                // fall through
            }
        }

        return try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, webUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            Log.w(TAG, "openChat fail=${e.javaClass.simpleName}")
            Toast.makeText(context, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun fileProviderAuthority(context: Context): String =
        "${context.packageName}.fileprovider"

    private fun ensureShareableContentUri(
        context: Context,
        sourceUri: Uri,
        mimeType: String,
        authority: String
    ): Uri? {
        if (sourceUri.scheme == "content" && sourceUri.authority == authority) {
            return if (canOpenUri(context, sourceUri)) sourceUri else null
        }

        return try {
            val dir = ReceiptImageStamper.prepareCacheDir(context)
            val ext = when {
                mimeType.contains("pdf", ignoreCase = true) -> "pdf"
                else -> "jpg"
            }
            val outFile = File(dir, "share_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            } ?: return null
            if (!outFile.exists() || outFile.length() <= 0L) {
                Log.e(TAG, "share file empty or missing")
                return null
            }
            val uri = FileProvider.getUriForFile(context, authority, outFile)
            if (uri.scheme != "content" || uri.authority != authority) {
                Log.e(TAG, "FileProvider authority mismatch")
                return null
            }
            if (canOpenUri(context, uri)) uri else null
        } catch (e: Exception) {
            Log.w(TAG, "ensureShareable fail=${e.javaClass.simpleName}")
            null
        }
    }

    private fun canOpenUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "openInputStream fail=${e.javaClass.simpleName}")
            false
        }
    }

    private fun resolveInstalledWhatsAppPackage(context: Context): String? {
        val pm = context.packageManager
        for (pkg in WHATSAPP_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: PackageManager.NameNotFoundException) {
                // try next
            }
        }
        return null
    }
}
