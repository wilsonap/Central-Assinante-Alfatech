package com.example.invoice

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import com.example.notifications.NotificationChannels
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Lógica única de lembretes de fatura — usada pelo Worker periódico e pela verificação imediata.
 */
object InvoiceReminderChecker {

    private const val TAG = "INVOICE_REMINDER"

    /**
     * @param trigger app_start | invoice_sync | worker
     */
    suspend fun run(context: Context, trigger: String) {
        val appContext = context.applicationContext
        if (trigger == "worker") {
            Log.i(TAG, "workerStarted=true")
        } else {
            Log.i(TAG, "immediate_check_started trigger=$trigger")
        }

        NotificationChannels.ensureCreated(appContext)

        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val today = iso.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = iso.format(cal.time)

        Log.i(TAG, "today=$today")

        val dao = AppDatabase.getDatabase(appContext).invoiceDao()
        val remindBefore = InvoiceReminderPrefs.isRemindDayBeforeEnabled(appContext)
        val remindToday = InvoiceReminderPrefs.isRemindDueDateEnabled(appContext)

        val candidates = mutableListOf<Pair<InvoiceEntity, String>>()
        if (remindBefore) {
            dao.findOpenByDueDate(tomorrow).forEach { inv ->
                candidates += inv to InvoiceReminderPrefs.KIND_DAY_BEFORE
            }
        }
        if (remindToday) {
            dao.findOpenByDueDate(today).forEach { inv ->
                candidates += inv to InvoiceReminderPrefs.KIND_DUE_DATE
            }
        }

        // Também conta abertas do dia/amanhã mesmo se preferência desligada (para log de elegibilidade).
        val allForLog = LinkedHashSet<InvoiceEntity>()
        allForLog.addAll(dao.findOpenByDueDate(today))
        allForLog.addAll(dao.findOpenByDueDate(tomorrow))
        Log.i(TAG, "invoicesChecked=${allForLog.size}")

        // Log detalhado de cada fatura do dia/amanhã (mesmo se preferência desligada).
        allForLog.forEach { inv ->
            val kind = when (inv.dueDate) {
                today -> InvoiceReminderPrefs.KIND_DUE_DATE
                tomorrow -> InvoiceReminderPrefs.KIND_DAY_BEFORE
                else -> "other"
            }
            val prefOn = when (kind) {
                InvoiceReminderPrefs.KIND_DUE_DATE -> remindToday
                InvoiceReminderPrefs.KIND_DAY_BEFORE -> remindBefore
                else -> false
            }
            val eligible = prefOn && !isClosed(inv)
            val key = InvoiceReminderPrefs.notificationKey(inv.idReceber, kind, inv.dueDate)
            val already = InvoiceReminderPrefs.wasFired(appContext, key)
            logCandidate(inv, kind, eligible, already)
        }

        candidates.forEach { (inv, kind) ->
            maybeNotify(appContext, inv, kind)
        }

        if (trigger == "worker") {
            Log.i(TAG, "workerFinished=true")
        }
    }

    private fun logCandidate(
        invoice: InvoiceEntity,
        kind: String,
        eligible: Boolean,
        alreadyNotified: Boolean
    ) {
        val kindLog = when (kind) {
            InvoiceReminderPrefs.KIND_DUE_DATE -> "due_today"
            InvoiceReminderPrefs.KIND_DAY_BEFORE -> "day_before"
            else -> kind
        }
        Log.i(TAG, "idReceber=${InvoiceParser.maskId(invoice.idReceber)}")
        Log.i(TAG, "dueDate=${invoice.dueDate}")
        Log.i(TAG, "status=${invoice.status}")
        Log.i(TAG, "eligible=$eligible")
        Log.i(TAG, "kind=$kindLog")
        Log.i(TAG, "alreadyNotified=$alreadyNotified")
    }

    private fun maybeNotify(context: Context, invoice: InvoiceEntity, kind: String) {
        val todayIsDue = kind == InvoiceReminderPrefs.KIND_DUE_DATE
        val kindLog = if (todayIsDue) "due_today" else "day_before"

        if (isClosed(invoice)) {
            Log.i(
                TAG,
                "idReceber=${InvoiceParser.maskId(invoice.idReceber)} eligible=false reason=closed"
            )
            return
        }

        val key = InvoiceReminderPrefs.notificationKey(invoice.idReceber, kind, invoice.dueDate)
        if (InvoiceReminderPrefs.wasFired(context, key)) {
            Log.i(TAG, "idReceber=${InvoiceParser.maskId(invoice.idReceber)} alreadyNotified=true kind=$kindLog")
            return
        }

        val title = if (todayIsDue) {
            "Sua fatura Alfatech vence hoje"
        } else {
            "Sua fatura Alfatech vence amanhã"
        }
        val valueLine = "Valor: ${formatBrl(invoice.amountCents)}"
        val tip = when (invoice.billingType) {
            InvoiceEntity.BILLING_BANK ->
                "Consulte o boleto na Central do Assinante."
            InvoiceEntity.BILLING_STORE ->
                "Procure um dos pontos de atendimento da Alfatech para realizar o pagamento."
            else ->
                "Acesse a Central do Assinante para consultar os detalhes."
        }
        val body = "$valueLine\n\n$tip"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_invoices", true)
        }
        val pending = PendingIntent.getActivity(
            context,
            key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.CHANNEL_INVOICES
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(valueLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(key.hashCode(), notification)
            InvoiceReminderPrefs.markFired(context, key)
            Log.i(TAG, "notificationPosted=true")
            Log.i(
                TAG,
                "fired kind=$kindLog idReceber=${InvoiceParser.maskId(invoice.idReceber)} " +
                    "billingType=${invoice.billingType}"
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "notify denied=${e.javaClass.simpleName}")
        }
    }

    private fun isClosed(invoice: InvoiceEntity): Boolean {
        val group = invoice.sourceGroup.orEmpty().lowercase(Locale.ROOT)
        if (group == InvoiceEntity.GROUP_PAGAS || group == InvoiceEntity.GROUP_CANCELADAS) {
            return true
        }
        val st = invoice.status.uppercase(Locale.ROOT)
        if (st in setOf("P", "C", "R")) return true
        val text = invoice.statusText.orEmpty().lowercase(Locale.ROOT)
        return text.contains("paga") || text.contains("pago") || text.contains("cancel")
    }

    private fun formatBrl(cents: Long): String {
        val reais = cents / 100
        val frac = (cents % 100).toInt().toString().padStart(2, '0')
        return "R$ $reais,$frac"
    }
}
