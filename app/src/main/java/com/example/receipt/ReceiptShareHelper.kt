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
     * Sem setPackage. Sem wa.me.
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

        if (!canOpenUri(context, shareUri)) {
            Log.e(TAG, "canOpenUri=false")
            Toast.makeText(context, "Não foi possível ler o comprovante.", Toast.LENGTH_SHORT).show()
            return false
        }

        val mime = if (mimeType.contains("pdf", ignoreCase = true)) {
            "application/pdf"
        } else {
            "image/*"
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_TEXT, message)
            clipData = ClipData.newRawUri("comprovante", shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
