package com.example

import android.app.Application
import com.example.invoice.InvoiceReminderPrefs
import com.example.invoice.InvoiceReminderScheduler
import com.example.notifications.NotificationChannels

/**
 * Canais + agenda periódica de lembretes.
 * NÃO dispara check imediato aqui — Application também sobe para WorkManager.
 * NÃO agenda teste DEBUG aqui (evita REPLACE reiniciando o delay).
 */
class AlfatechApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
        InvoiceReminderPrefs.hydrate(this)
        com.example.branding.CompanyLogoStore.hydrate(this)
        InvoiceReminderScheduler.schedulePeriodic(this)
    }
}
