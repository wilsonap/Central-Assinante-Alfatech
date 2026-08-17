package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PushNotificationRepository
import com.example.notifications.NotificationChannels
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Armazenamento local simples do token FCM atual.
 * Atualizado por getToken / onNewToken. A Central associa o token via
 * ACTION=updateFirebaseToken (dados_cliente.php) — sem POST inventado aqui.
 */
object FcmTokenStore {
    private const val PREFS = "alfatech_fcm_prefs"
    private const val KEY_TOKEN = "fcm_token"

    @Volatile
    private var memoryToken: String = ""

    fun update(context: Context, token: String) {
        if (token.isBlank()) return
        memoryToken = token
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun current(context: Context): String {
        if (memoryToken.isNotEmpty()) return memoryToken
        memoryToken = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, "")
            .orEmpty()
        return memoryToken
    }

    fun mask(token: String): String {
        if (token.isBlank()) return "(vazio)"
        if (token.length <= 12) return "***"
        return token.take(6) + "..." + token.takeLast(4)
    }
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenStore.update(applicationContext, token)
        Log.i(TAG, "onNewToken - token local atualizado: ${FcmTokenStore.mask(token)}")
        Log.i("FCM_TOKEN_REGISTERED", "local_store updated token=${FcmTokenStore.mask(token)}")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Alfatech Telecom"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Você possui uma nova mensagem na Central."

        val type = remoteMessage.data["type"] ?: "general"

        val targetUrl = remoteMessage.data["url"]
            ?: remoteMessage.data["target_url"]
            ?: "https://sac2.alfatechtelecom.com.br/central_assinante_web/"

        val messageId = remoteMessage.messageId
            ?: remoteMessage.data["message_id"]
            ?: remoteMessage.data["google.message_id"]

        val hasNotificationPayload = remoteMessage.notification != null
        val hasDataPayload = remoteMessage.data.isNotEmpty()
        val payloadKind = when {
            hasNotificationPayload && hasDataPayload -> "notification+data"
            hasNotificationPayload -> "notification"
            else -> "data-only"
        }

        Log.i(TAG, "FCM MESSAGE RECEIVED payloadKind=$payloadKind messageId=$messageId")
        Log.i(TAG, "notification.title: ${remoteMessage.notification?.title}")
        Log.i(TAG, "notification.body: ${remoteMessage.notification?.body}")
        Log.i(TAG, "remoteMessage.data: ${remoteMessage.data}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                PushNotificationRepository.persistFromPush(
                    context = applicationContext,
                    title = title,
                    body = body,
                    type = type,
                    messageId = messageId,
                    targetUrl = targetUrl
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar notificação no banco de dados local", e)
            }
        }

        // onMessageReceived só roda quando o sistema NÃO exibiu a notificação
        // (foreground com notification, ou data-only). Evita bandeja duplicada.
        showNotification(title, body, type, messageId, targetUrl)
    }

    private fun showNotification(
        title: String,
        body: String,
        type: String,
        messageId: String?,
        url: String
    ) {
        NotificationChannels.ensureCreated(this)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = NotificationChannels.CHANNEL_CENTRAL

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_url", url)
            putExtra("title", title)
            putExtra("body", body)
            putExtra("type", type)
            if (!messageId.isNullOrBlank()) {
                putExtra("message_id", messageId)
                putExtra("google.message_id", messageId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (messageId?.hashCode() ?: System.currentTimeMillis().toInt()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (messageId?.hashCode() ?: (System.currentTimeMillis() % 100000).toInt())
        notificationManager.notify(notificationId, notification)
        Log.i(TAG, "Notificação exibida no Android - ID: $notificationId")
    }

    companion object {
        private const val TAG = "AlfatechFCM"
    }
}
