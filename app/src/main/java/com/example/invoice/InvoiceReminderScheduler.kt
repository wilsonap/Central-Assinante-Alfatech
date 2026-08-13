package com.example.invoice

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object InvoiceReminderScheduler {
    const val UNIQUE_PERIODIC_NAME = "invoice_due_reminder_periodic"

    fun schedulePeriodic(context: Context) {
        try {
            val request = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag("INVOICE_REMINDER")
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i("INVOICE_REMINDER", "periodic scheduled name=$UNIQUE_PERIODIC_NAME")
        } catch (e: IllegalStateException) {
            // WorkManager ainda não inicializado (ex.: testes unitários / processo sem initializer).
            Log.w("INVOICE_REMINDER", "schedule skipped=${e.javaClass.simpleName}")
        }
    }
}
