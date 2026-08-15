package com.example.invoice

import android.content.Context
import android.content.SharedPreferences

/**
 * Diagnóstico persistente de lembretes — sem PII / sem conteúdo de fatura.
 */
object InvoiceReminderDiagnostics {
    private const val PREFS = "invoice_reminder_diagnostics"
    private const val KEY_LAST_WORKER_RUN = "lastWorkerRunAt"
    private const val KEY_LAST_CHECK = "lastReminderCheckAt"
    private const val KEY_LAST_ATTEMPT = "lastNotificationAttemptAt"
    private const val KEY_LAST_POSTED = "lastNotificationPostedAt"
    private const val KEY_LAST_SKIP = "lastSkipReason"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun markWorkerRun(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_WORKER_RUN, System.currentTimeMillis()).apply()
    }

    fun markCheck(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    fun markAttempt(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_ATTEMPT, System.currentTimeMillis()).apply()
    }

    fun markPosted(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_POSTED, System.currentTimeMillis()).apply()
    }

    fun markSkip(context: Context, reason: String) {
        prefs(context).edit().putString(KEY_LAST_SKIP, reason).apply()
    }

    fun snapshot(context: Context): Map<String, String> {
        val p = prefs(context)
        return mapOf(
            "lastWorkerRunAt" to p.getLong(KEY_LAST_WORKER_RUN, 0L).toString(),
            "lastReminderCheckAt" to p.getLong(KEY_LAST_CHECK, 0L).toString(),
            "lastNotificationAttemptAt" to p.getLong(KEY_LAST_ATTEMPT, 0L).toString(),
            "lastNotificationPostedAt" to p.getLong(KEY_LAST_POSTED, 0L).toString(),
            "lastSkipReason" to (p.getString(KEY_LAST_SKIP, "") ?: "")
        )
    }
}
