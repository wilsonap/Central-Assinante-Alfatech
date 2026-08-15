package com.example.invoice

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Agenda lembretes individuais por fatura via AlarmManager.setWindow (11h–13h).
 * Não usa alarmes exatos.
 */
object InvoiceAlarmScheduler {

    private const val TAG = "INVOICE_ALARM"
    private const val RC_PREFS = "invoice_alarm_request_codes"
    private const val KEY_NEXT_RC = "_next_request_code"

    const val ACTION_REMINDER = "com.example.invoice.ACTION_INVOICE_REMINDER"
    const val EXTRA_ID_RECEBER = "idReceber"
    const val EXTRA_KIND = "kind"
    const val EXTRA_DUE_DATE = "dueDate"

    fun scheduleInvoice(context: Context, invoice: InvoiceEntity) {
        if (InvoiceAlarmTiming.isTerminalInvoice(invoice)) {
            cancelInvoice(context, invoice)
            return
        }
        scheduleKind(context, invoice.idReceber, InvoiceReminderPrefs.KIND_DAY_BEFORE, invoice.dueDate)
        scheduleKind(context, invoice.idReceber, InvoiceReminderPrefs.KIND_DUE_DATE, invoice.dueDate)
    }

    fun cancelInvoice(context: Context, invoice: InvoiceEntity) {
        cancelKind(context, invoice.idReceber, InvoiceReminderPrefs.KIND_DAY_BEFORE, invoice.dueDate)
        cancelKind(context, invoice.idReceber, InvoiceReminderPrefs.KIND_DUE_DATE, invoice.dueDate)
    }

    fun rescheduleInvoice(context: Context, oldInvoice: InvoiceEntity, newInvoice: InvoiceEntity) {
        Log.i(
            TAG,
            "reschedule idReceber=${InvoiceParser.maskId(newInvoice.idReceber)} " +
                "oldDueDate=${oldInvoice.dueDate} newDueDate=${newInvoice.dueDate}"
        )
        cancelInvoice(context, oldInvoice)
        scheduleInvoice(context, newInvoice)
    }

    suspend fun scheduleAllOpenInvoices(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val open = AppDatabase.getDatabase(appContext).invoiceDao().getOpenInvoices()
        open.forEach { scheduleInvoice(appContext, it) }
        Log.i(TAG, "bootReschedule count=${open.size}")
    }

    fun scheduleKind(context: Context, idReceber: String, kind: String, dueDate: String) {
        val appContext = context.applicationContext
        val target = InvoiceAlarmTiming.alarmTargetDate(dueDate, kind) ?: return
        val window = InvoiceAlarmTiming.computeWindow(target) ?: run {
            Log.i(
                TAG,
                "schedule skip pastWindow idReceber=${InvoiceParser.maskId(idReceber)} " +
                    "kind=$kind dueDate=$dueDate target=$target"
            )
            return
        }
        val (windowStart, windowLength) = window
        val windowEnd = windowStart + windowLength

        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(appContext, idReceber, kind, dueDate, create = true)
            ?: return

        am.setWindow(AlarmManager.RTC_WAKEUP, windowStart, windowLength, pi)

        Log.i(TAG, "schedule")
        Log.i(TAG, "idReceber=${InvoiceParser.maskId(idReceber)}")
        Log.i(TAG, "kind=$kind")
        Log.i(TAG, "dueDate=$dueDate")
        Log.i(TAG, "windowStart=$windowStart")
        Log.i(TAG, "windowEnd=$windowEnd")
    }

    fun cancelKind(context: Context, idReceber: String, kind: String, dueDate: String) {
        val appContext = context.applicationContext
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(appContext, idReceber, kind, dueDate, create = false) ?: return
        am.cancel(pi)
        pi.cancel()
        Log.i(TAG, "cancel")
        Log.i(TAG, "idReceber=${InvoiceParser.maskId(idReceber)}")
        Log.i(TAG, "kind=$kind")
        Log.i(TAG, "dueDate=$dueDate")
    }

    fun reminderUri(idReceber: String, kind: String, dueDate: String): Uri =
        Uri.parse("alfatech://invoice-reminder/$idReceber/$kind/$dueDate")

    fun alarmKey(idReceber: String, kind: String, dueDate: String): String =
        InvoiceReminderPrefs.notificationKey(idReceber, kind, dueDate)

    /**
     * Request code estável por chave, alocado em SharedPreferences (evita colisão silenciosa de hash).
     */
    fun requestCode(context: Context, idReceber: String, kind: String, dueDate: String): Int {
        val key = alarmKey(idReceber, kind, dueDate)
        val prefs = context.applicationContext.getSharedPreferences(RC_PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getInt(key, 0)
        if (existing != 0) return existing
        synchronized(this) {
            val again = prefs.getInt(key, 0)
            if (again != 0) return again
            var next = prefs.getInt(KEY_NEXT_RC, 10_000) + 1
            // Evita 0 (sentinel "não alocado").
            if (next == 0) next = 10_001
            prefs.edit().putInt(KEY_NEXT_RC, next).putInt(key, next).apply()
            return next
        }
    }

    private fun pendingIntent(
        context: Context,
        idReceber: String,
        kind: String,
        dueDate: String,
        create: Boolean
    ): PendingIntent? {
        val intent = Intent(context, InvoiceReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            data = reminderUri(idReceber, kind, dueDate)
            putExtra(EXTRA_ID_RECEBER, idReceber)
            putExtra(EXTRA_KIND, kind)
            putExtra(EXTRA_DUE_DATE, dueDate)
        }
        val flags = if (create) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        val rc = requestCode(context, idReceber, kind, dueDate)
        return PendingIntent.getBroadcast(context, rc, intent, flags)
    }

    /** Visível para testes: flags de API / setWindow disponível. */
    fun usesInexactWindow(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}
