package com.example.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

/**
 * Captura/abertura do WhatsApp de atendimento configurado na Central IXC.
 * Número nunca é hardcoded; vem do DOM/link da Central.
 */
object WhatsAppSupport {

    const val DEFAULT_MESSAGE = "Olá! Preciso de atendimento."
    private const val TAG = "WHATSAPP_CONFIG"

    data class Config(
        val number: String,
        val message: String,
        val fullUrl: String
    )

    /**
     * Mensagem do atalho "Chamar no WhatsApp".
     * Reutiliza os mesmos campos já capturados para Enviar comprovante
     * ([MainViewModel.clientFullName], [MainViewModel.clientCode], [MainViewModel.clientContract]).
     * Campos vazios são omitidos; não bloqueia se o perfil ainda não carregou.
     */
    fun buildSupportMessage(
        fullName: String = "",
        clientCode: String = "",
        contract: String = ""
    ): String {
        val name = fullName.trim()
        val code = clientCode.trim()
        val contrato = contract.trim()
        return buildString {
            append("Olá, Alfatech Telecom!\n")
            append("Preciso de atendimento.\n")
            if (name.isNotBlank() || code.isNotBlank() || contrato.isNotBlank()) {
                append('\n')
                if (name.isNotBlank()) append("Cliente: ").append(name).append('\n')
                if (code.isNotBlank()) append("Código: ").append(code).append('\n')
                if (contrato.isNotBlank()) append("Contrato: ").append(contrato).append('\n')
            }
            append("\nEnviado pelo app Central do Assinante Alfatech.")
        }
    }

    fun parseFromHref(href: String?): Config? {
        if (href.isNullOrBlank()) return null
        val uri = try {
            Uri.parse(href.trim())
        } catch (_: Exception) {
            return null
        }

        val host = uri.host.orEmpty().lowercase()
        val number = when {
            host.contains("api.whatsapp.com") ||
                href.contains("api.whatsapp.com/send", ignoreCase = true) -> {
                uri.getQueryParameter("phone").orEmpty().filter { it.isDigit() }
            }
            host.contains("wa.me") || href.contains("wa.me/", ignoreCase = true) -> {
                val path = uri.path.orEmpty().trim('/')
                path.substringBefore('/').filter { it.isDigit() }
                    .ifEmpty {
                        uri.getQueryParameter("phone").orEmpty().filter { it.isDigit() }
                    }
            }
            href.startsWith("whatsapp://", ignoreCase = true) -> {
                uri.getQueryParameter("phone").orEmpty().filter { it.isDigit() }
            }
            else -> ""
        }

        if (number.isEmpty()) return null

        val message = uri.getQueryParameter("text")
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_MESSAGE

        return Config(number = number, message = message, fullUrl = href.trim())
    }

    fun maskNumber(number: String): String {
        val digits = number.filter { it.isDigit() }
        if (digits.length <= 4) return "****"
        return "*".repeat((digits.length - 4).coerceAtLeast(4)) + digits.takeLast(4)
    }

    fun logCaptured(number: String, source: String) {
        Log.i(TAG, "number=${maskNumber(number)} source=$source")
    }

    fun openChat(
        context: Context,
        number: String?,
        message: String?,
        fullUrl: String?
    ): Boolean {
        val digits = number?.filter { it.isDigit() }.orEmpty()
        val text = message?.takeIf { it.isNotBlank() } ?: DEFAULT_MESSAGE

        val webUri = when {
            digits.isNotEmpty() -> {
                Uri.parse(
                    "https://api.whatsapp.com/send?phone=$digits&text=${Uri.encode(text)}"
                )
            }
            !fullUrl.isNullOrBlank() -> Uri.parse(fullUrl)
            else -> {
                Toast.makeText(
                    context,
                    "WhatsApp de atendimento ainda não disponível. Abra a Central do Assinante e tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
                Log.i(TAG, "open skipped — no number/url yet")
                return false
            }
        }

        val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
        for (pkg in packages) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.i(TAG, "opened via package=$pkg number=${maskNumber(digits)}")
                return true
            } catch (_: ActivityNotFoundException) {
                // try next
            } catch (_: Exception) {
                // try next
            }
        }

        return try {
            val fallback = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            Log.i(TAG, "opened via web fallback number=${maskNumber(digits)}")
            true
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "Não foi possível abrir o WhatsApp.",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
}
