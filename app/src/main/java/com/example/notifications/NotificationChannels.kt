package com.example.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.R

/**
 * Ponto único de criação dos canais de notificação.
 *
 * - [CHANNEL_CENTRAL]: canal oficial do app / Manifest FCM
 * - [CHANNEL_DEFAULT]: compatibilidade com backend que envia channel_id = "default"
 *
 * Em API &lt; 26 os canais não existem; NotificationCompat usa o canal id no Builder
 * sem chamar APIs de [NotificationChannel].
 *
 * Em Android 8+, createNotificationChannel NÃO sobrescreve importância/vibração
 * de canal já existente no aparelho.
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
    private const val TAG_DIAG = "NOTIFICATION_CHANNEL"
    private const val TAG_INVOICE = "INVOICE_REMINDER"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        ensureCreatedOreo(context.applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ensureCreatedOreo(appContext: Context) {
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return

        val description = appContext.getString(R.string.default_notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val centralName = appContext.getString(R.string.default_notification_channel_name)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CENTRAL, centralName, importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )

        // Android 8+: createNotificationChannel NÃO altera vibração/importância de canal já existente.
        logInvoiceChannelStateOreo(manager)

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
     * Diagnóstico de apresentação ao abrir o app.
     * Não tenta modificar canal já criado pelo usuário.
     */
    fun logPresentationDiagnostics(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.applicationContext
            .getSystemService(NotificationManager::class.java) ?: return
        listOf(CHANNEL_CENTRAL, CHANNEL_INVOICES).forEach { id ->
            logChannelPresentationOreo(manager, id)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun logChannelPresentationOreo(manager: NotificationManager, channelId: String) {
        val channel = manager.getNotificationChannel(channelId)
        if (channel == null) {
            Log.i(TAG_DIAG, "id=$channelId")
            Log.i(TAG_DIAG, "importance=-1")
            Log.i(TAG_DIAG, "vibration=false")
            Log.i(TAG_DIAG, "lockscreenVisibility=-1")
            Log.i(TAG_DIAG, "showBadge=false")
            Log.i(TAG_DIAG, "headsUpPossible=false reason=channel_missing")
            return
        }
        val importance = channel.importance
        Log.i(TAG_DIAG, "id=$channelId")
        Log.i(TAG_DIAG, "importance=$importance")
        Log.i(TAG_DIAG, "vibration=${channel.shouldVibrate()}")
        Log.i(TAG_DIAG, "lockscreenVisibility=${channel.lockscreenVisibility}")
        Log.i(TAG_DIAG, "showBadge=${channel.canShowBadge()}")
        when {
            importance == NotificationManager.IMPORTANCE_NONE -> {
                Log.i(TAG_DIAG, "headsUpPossible=false reason=channel_blocked")
            }
            importance < NotificationManager.IMPORTANCE_HIGH -> {
                Log.i(TAG_DIAG, "headsUpPossible=false reason=importance_lowered")
            }
            else -> {
                Log.i(TAG_DIAG, "headsUpPossible=true")
            }
        }
    }

    /**
     * Diagnóstico do canal de faturas. Em Android 8+, vibração efetiva vem do canal já criado
     * no aparelho — código novo não sobrescreve preferências existentes.
     */
    fun logInvoiceChannelState(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        logInvoiceChannelStateOreo(manager)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun logInvoiceChannelStateOreo(manager: NotificationManager) {
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
