package com.example.invoice

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Fallback de segurança (12h): detecta avisos devidos não disparados.
 * Mecanismo principal = [InvoiceAlarmScheduler].
 */
object InvoiceReminderScheduler {
    const val UNIQUE_PERIODIC_NAME = "invoice_due_reminder_periodic_12h"
    private const val LEGACY_PERIODIC_NAME = "invoice_due_reminder_periodic"

    fun schedulePeriodic(context: Context) {
        try {
            val wm = WorkManager.getInstance(context.applicationContext)
            wm.cancelUniqueWork(LEGACY_PERIODIC_NAME)
            // Remove OneTime DEBUG antigo, se existir.
            wm.cancelUniqueWork("invoice_reminder_debug_test")

            val request = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag("INVOICE_REMINDER")
                .build()

            wm.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i(
                "INVOICE_REMINDER",
                "periodic fallback scheduled name=$UNIQUE_PERIODIC_NAME intervalHours=12 policy=KEEP"
            )
        } catch (e: IllegalStateException) {
            Log.w("INVOICE_REMINDER", "schedule skipped=${e.javaClass.simpleName}")
        }
    }
}
