package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationEntity
import java.security.MessageDigest

/**
 * Ponto único de persistência de push → Room (Avisos e Comunicados).
 * Usado por [com.example.service.MyFirebaseMessagingService] e [com.example.MainActivity].
 */
object PushNotificationRepository {

    private const val TAG = "FCM_PERSIST"
    private const val DEDUPE_WINDOW_MS = 120_000L // 2 minutos

    /**
     * @return true se inseriu, false se ignorou (duplicata / conteúdo vazio)
     */
    suspend fun persistFromPush(
        context: Context,
        title: String?,
        body: String?,
        type: String?,
        messageId: String?,
        targetUrl: String?
    ): Boolean {
        val resolvedTitle = title?.trim().orEmpty().ifBlank { "Alfatech Telecom" }
        val resolvedBody = body?.trim().orEmpty().ifBlank {
            "Você possui uma nova mensagem na Central."
        }
        val resolvedType = type?.trim().orEmpty().ifBlank { "general" }
        val mid = messageId?.trim()?.takeIf { it.isNotEmpty() }
        val url = targetUrl?.trim()?.takeIf { it.isNotEmpty() }
        val contentHash = sha256Hex("$resolvedTitle|$resolvedBody|$resolvedType")

        val dao = AppDatabase.getDatabase(context).notificationDao()

        if (mid != null) {
            if (dao.findByMessageId(mid) != null) {
                Log.i(TAG, "dedupe hit messageId=$mid — skip insert")
                return false
            }
        } else {
            val since = System.currentTimeMillis() - DEDUPE_WINDOW_MS
            if (dao.findRecentByContentHash(contentHash, since) != null) {
                Log.i(TAG, "dedupe hit contentHash window — skip insert")
                return false
            }
        }

        val rowId = dao.insertNotification(
            NotificationEntity(
                title = resolvedTitle,
                body = resolvedBody,
                type = resolvedType,
                messageId = mid,
                contentHash = contentHash,
                targetUrl = url
            )
        )

        // IGNORE returns -1 on unique conflict (race)
        if (rowId == -1L) {
            Log.i(TAG, "dedupe race messageId=$mid — skip insert")
            return false
        }

        Log.i(
            TAG,
            "inserted id=$rowId messageId=$mid type=$resolvedType hasUrl=${url != null}"
        )
        return true
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
