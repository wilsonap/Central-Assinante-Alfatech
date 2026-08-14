package com.example.invoice

import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker periódico — delega a lógica ao [InvoiceReminderChecker].
 */
class InvoiceReminderWorker(
    appContext: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            InvoiceReminderChecker.run(applicationContext, trigger = "worker")
            Result.success()
        } catch (e: Exception) {
            Log.w("INVOICE_REMINDER", "worker fail=${e.javaClass.simpleName}")
            Result.retry()
        }
    }
}
