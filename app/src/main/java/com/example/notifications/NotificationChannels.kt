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
    const val CHANNEL_INVOICES = "invoice_reminders_channel"
    /** Preparado para futura notificação com app fechado (Fase 2). */
    const val CHANNEL_APP_UPDATES = "app_updates_channel"

    /** Padrão de vibração dos lembretes (também no Builder pré-O / reforço). */
    val INVOICE_VIBRATION_PATTERN = longArrayOf(0, 300, 200, 300)

    private const val TAG = "FCM_CHANNELS"
    private const val TAG_INVOICE = "INVOICE_REMINDER"

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

        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_INVOICES, "Lembretes de faturas", importance).apply {
                this.description = "Avisos de vencimento de faturas Alfatech"
                enableVibration(true)
                vibrationPattern = INVOICE_VIBRATION_PATTERN
                enableLights(true)
                setShowBadge(true)
            }
        )

        // Android 8+: createNotificationChannel NÃO altera vibração/importância de canal já existente.
        logInvoiceChannelState(manager)

        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_APP_UPDATES, "Atualizações do aplicativo", importance).apply {
                this.description = "Avisos quando houver nova versão do aplicativo Alfatech"
                enableVibration(true)
            }
        )

        Log.i(
            TAG,
            "channels ready: $CHANNEL_CENTRAL + $CHANNEL_DEFAULT + $CHANNEL_INVOICES + $CHANNEL_APP_UPDATES"
        )
    }

    /**
     * Diagnóstico do canal de faturas. Em Android 8+, vibração efetiva vem do canal já criado
     * no aparelho — código novo não sobrescreve preferências existentes.
     */
    fun logInvoiceChannelState(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = manager.getNotificationChannel(CHANNEL_INVOICES)
        if (channel == null) {
            Log.i(TAG_INVOICE, "channelVibrationEnabled=false")
            Log.i(TAG_INVOICE, "channelImportance=-1")
            return
        }
        Log.i(TAG_INVOICE, "channelVibrationEnabled=${channel.shouldVibrate()}")
        Log.i(TAG_INVOICE, "channelImportance=${channel.importance}")
    }
}
