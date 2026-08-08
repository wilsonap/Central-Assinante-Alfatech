package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationEntity
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

        val targetUrl = remoteMessage.data["url"]
            ?: "https://sac2.alfatechtelecom.com.br/central_assinante_web/"

        Log.i(TAG, "FCM MESSAGE RECEIVED")
        Log.i(TAG, "notification.title: ${remoteMessage.notification?.title}")
        Log.i(TAG, "notification.body: ${remoteMessage.notification?.body}")
        Log.i(TAG, "remoteMessage.data: ${remoteMessage.data}")
        Log.i(TAG, "FCM MESSAGE RECEIVED - Title: '$title' | Body: '$body' | Data: ${remoteMessage.data}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        title = title,
                        body = body,
                        type = remoteMessage.data["type"] ?: "general"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar notificação no banco de dados local", e)
            }
        }

        showNotification(title, body, targetUrl)
    }

    private fun showNotification(title: String, body: String, url: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.default_notification_channel_id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.default_notification_channel_desc)
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_url", url)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notification)
        Log.i(TAG, "Notificação exibida no Android - ID: $notificationId")
    }

    companion object {
        private const val TAG = "AlfatechFCM"
    }
}
