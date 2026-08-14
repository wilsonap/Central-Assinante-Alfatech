package com.example

import android.app.Application
import com.example.invoice.InvoiceReminderChecker
import com.example.invoice.InvoiceReminderPrefs
import com.example.invoice.InvoiceReminderScheduler
import com.example.notifications.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Garante criação precoce dos NotificationChannels (inclui ID "default" do backend)
 * e agenda verificação diária de vencimento de faturas (janela flexível).
 * Também dispara verificação imediata na abertura do processo.
 */
class AlfatechApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
        InvoiceReminderPrefs.hydrate(this)
        InvoiceReminderScheduler.schedulePeriodic(this)
        appScope.launch {
            try {
                InvoiceReminderChecker.run(this@AlfatechApp, trigger = "app_start")
            } catch (_: Exception) {
                // Não bloqueia startup; Worker periódico permanece como fallback.
            }
        }
    }
}
