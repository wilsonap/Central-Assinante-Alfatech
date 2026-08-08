package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.R

/**
 * Ponto único de criação dos canais de notificação.
 *
 * - [CHANNEL_CENTRAL]: canal oficial do app / Manifest FCM
 * - [CHANNEL_DEFAULT]: compatibilidade com backend que envia channel_id = "default"
 *
 * Resolver o warning de canal NÃO altera persistência no Room.
 */
object NotificationChannels {

    const val CHANNEL_CENTRAL = "central_alfatech_channel"
    const val CHANNEL_DEFAULT = "default"

    private const val TAG = "FCM_CHANNELS"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val appContext = context.applicationContext
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return

        val description = appContext.getString(R.string.default_notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val centralName = appContext.getString(R.string.default_notification_channel_name)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CENTRAL, centralName, importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
            }
        )

        // Backend legado solicita ID literal "default"
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_DEFAULT, "Notificações", importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
            }
        )

        Log.i(
            TAG,
            "channels ready: $CHANNEL_CENTRAL + $CHANNEL_DEFAULT (importance=HIGH)"
        )
    }
}
