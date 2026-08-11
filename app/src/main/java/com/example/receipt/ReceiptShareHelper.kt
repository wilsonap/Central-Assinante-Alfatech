package com.example.receipt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Compartilhamento de comprovante.
 *
 * EXTRA_STREAM = mesma [selectedUri] original (sem stamped / sem copia).
 * EXTRA_TEXT   = dados do cliente.
 *
 * Imagem e PDF usam a mesma [shareFileToWhatsApp] (ACTION_SEND + chooser).
 */
object ReceiptShareHelper {

    private const val TAG = "RECEIPT_SHARE"
    private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

    fun normalizePhone(raw: String?): String =
        raw.orEmpty().filter { it.isDigit() }

    /** Identificacao curta para comparar URIs sem expor o caminho completo. */
    fun uriTraceId(uri: Uri): String {
        val raw = listOf(
            uri.scheme.orEmpty(),
            uri.authority.orEmpty(),
            uri.lastPathSegment.orEmpty(),
            uri.toString().length.toString()
        ).joinToString("|")
        return Integer.toHexString(raw.hashCode())
    }

    fun buildMessage(
        fullName: String,
        clientCode: String = "",
        contract: String = ""
    ): String {
        val whenStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())
        return buildString {
            append("Olá, Alfatech Telecom.\n\n")
            append("Segue meu comprovante de pagamento.\n")
            if (fullName.isNotBlank()) {
                append("\nCliente: ").append(fullName.trim())
            }
            if (clientCode.isNotBlank()) {
                append("\nCódigo: ").append(clientCode.trim())
            }
            if (contract.isNotBlank()) {
                append("\nContrato: ").append(contract.trim())
            }
            append("\nEnviado em: ").append(whenStr)
            append("\n\nEnviado pelo app Central do Assinante Alfatech.")
        }
    }

    fun createCameraCaptureUri(context: Context): Uri {
        val dir = File(context.cacheDir, "receipts").also { if (!it.exists()) it.mkdirs() }
        val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun resolveMime(context: Context, uri: Uri, fallback: String): String {
        val fromResolver = runCatching { context.contentResolver.getType(uri) }.getOrNull()
        if (!fromResolver.isNullOrBlank()) return fromResolver
        if (fallback.contains("pdf", ignoreCase = true)) return "application/pdf"
        if (fallback.startsWith("image/", ignoreCase = true)) return fallback
        return "image/*"
    }

    /**
     * Compartilha comprovante (imagem ou PDF) pelo mesmo ACTION_SEND + chooser.
     * EXTRA_STREAM = selectedUri original; EXTRA_TEXT = mensagem do cliente.
     * A unica diferenca e o MIME (image/jpeg|image/png|... vs application/pdf).
     */
    fun shareFileToWhatsApp(
        context: Context,
        selectedUri: Uri,
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

        if (selectedUri.scheme != "content") {
            Log.e(TAG, "scheme invalido — somente content://")
            Toast.makeText(context, "URI do comprovante inválida.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!canOpenUri(context, selectedUri)) {
            Log.e(TAG, "canOpenUri=false")
            Toast.makeText(context, "Não foi possível ler o comprovante.", Toast.LENGTH_SHORT).show()
            return false
        }

        val resolvedMime = resolveMime(context, selectedUri, mimeType)
        val kind = if (
            resolvedMime.contains("pdf", ignoreCase = true) ||
            mimeType.contains("pdf", ignoreCase = true)
        ) {
            "pdf"
        } else {
            "image"
        }

        val shareId = uriTraceId(selectedUri)
        Log.i(TAG, "RECEIPT_SHARE_URI id=$shareId")

        for (pkg in WHATSAPP_PACKAGES) {
            runCatching {
                context.grantUriPermission(pkg, selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = resolvedMime
            putExtra(Intent.EXTRA_STREAM, selectedUri)
            putExtra(Intent.EXTRA_TEXT, message)
            clipData = ClipData.newRawUri("comprovante", selectedUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Log.i("RECEIPT_SHARE_FLOW", "kind=$kind")
        Log.i("RECEIPT_SHARE_FLOW", "action=SEND")
        Log.i("RECEIPT_SHARE_FLOW", "hasStream=true")
        Log.i("RECEIPT_SHARE_FLOW", "hasText=true")
        Log.i("RECEIPT_SHARE_FLOW", "hasClipData=${intent.clipData != null}")

        // PDF: WhatsApp abre "Adicione uma legenda..." e ignora EXTRA_TEXT — copia a mensagem.
        if (kind == "pdf") {
            runCatching {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("comprovante_mensagem", message))
            }.onFailure {
                Log.w(TAG, "clipboard fail=${it.javaClass.simpleName}")
            }
            Toast.makeText(
                context,
                "Mensagem copiada. No WhatsApp, toque em \"Adicione uma legenda...\" e cole.",
                Toast.LENGTH_LONG
            ).show()
        }

        return try {
            context.startActivity(Intent.createChooser(intent, "Enviar comprovante"))
            true
        } catch (e: Exception) {
            Log.w(TAG, "share fail=${e.javaClass.simpleName}")
            Toast.makeText(context, "Não foi possível abrir o compartilhamento.", Toast.LENGTH_SHORT).show()
            false
        }
    }

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
