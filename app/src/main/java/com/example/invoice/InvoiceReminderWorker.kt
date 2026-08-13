package com.example.invoice

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import com.example.notifications.NotificationChannels
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InvoiceReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            NotificationChannels.ensureCreated(applicationContext)
            val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            val today = iso.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = iso.format(cal.time)
            val dao = AppDatabase.getDatabase(applicationContext).invoiceDao()

            val remindBefore = InvoiceReminderPrefs.isRemindDayBeforeEnabled(applicationContext)
            val remindToday = InvoiceReminderPrefs.isRemindDueDateEnabled(applicationContext)

            if (remindBefore) {
                dao.findOpenByDueDate(tomorrow).forEach { inv ->
                    maybeNotify(inv, InvoiceReminderPrefs.KIND_DAY_BEFORE, todayIsDue = false)
                }
            }
            if (remindToday) {
                dao.findOpenByDueDate(today).forEach { inv ->
                    maybeNotify(inv, InvoiceReminderPrefs.KIND_DUE_DATE, todayIsDue = true)
                }
            }
            Log.i("INVOICE_REMINDER", "worker done syncSuccess=true")
            Result.success()
        } catch (e: Exception) {
            Log.w("INVOICE_REMINDER", "worker fail=${e.javaClass.simpleName}")
            Result.retry()
        }
    }

    private fun maybeNotify(invoice: InvoiceEntity, kind: String, todayIsDue: Boolean) {
        if (isClosed(invoice)) {
            Log.i(
                "INVOICE_REMINDER",
                "skip closed idReceber=${InvoiceParser.maskId(invoice.idReceber)} status=${invoice.status}"
            )
            return
        }
        val key = InvoiceReminderPrefs.notificationKey(invoice.idReceber, kind, invoice.dueDate)
        if (InvoiceReminderPrefs.wasFired(applicationContext, key)) {
            Log.i("INVOICE_REMINDER", "skip duplicate key=$kind id=${InvoiceParser.maskId(invoice.idReceber)}")
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

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_invoices", true)
        }
        val pending = PendingIntent.getActivity(
            applicationContext,
            key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
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
            NotificationManagerCompat.from(applicationContext)
                .notify(key.hashCode(), notification)
            InvoiceReminderPrefs.markFired(applicationContext, key)
            Log.i(
                "INVOICE_REMINDER",
                "fired kind=$kind idReceber=${InvoiceParser.maskId(invoice.idReceber)} " +
                    "billingType=${invoice.billingType}"
            )
        } catch (e: SecurityException) {
            Log.w("INVOICE_REMINDER", "notify denied=${e.javaClass.simpleName}")
        }
    }

    private fun isClosed(invoice: InvoiceEntity): Boolean {
        val group = invoice.sourceGroup.orEmpty().lowercase()
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
