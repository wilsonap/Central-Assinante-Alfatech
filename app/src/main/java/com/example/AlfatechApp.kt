package com.example

import android.app.Application
import com.example.invoice.InvoiceReminderPrefs
import com.example.invoice.InvoiceReminderScheduler
import com.example.notifications.NotificationChannels

/**
 * Garante criação precoce dos NotificationChannels (inclui ID "default" do backend)
 * e agenda verificação diária de vencimento de faturas (janela flexível).
 */
class AlfatechApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
        InvoiceReminderPrefs.hydrate(this)
        InvoiceReminderScheduler.schedulePeriodic(this)
    }
}
