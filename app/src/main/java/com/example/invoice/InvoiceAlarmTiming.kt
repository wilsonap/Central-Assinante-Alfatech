package com.example.invoice

import com.example.data.local.InvoiceEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Trigger de lembrete: 12:00 local no dia-alvo (setAndAllowWhileIdle).
 * Após 13:00 do dia-alvo → não agenda (sem retroativo).
 */
object InvoiceAlarmTiming {

    const val TRIGGER_HOUR = 12
    const val CUTOFF_HOUR = 13
    /** Se já passou das 12:00 e ainda < 13:00, agenda daqui a poucos minutos. */
    const val SOON_DELAY_MS = 3L * 60L * 1000L

    private val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val localStamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)

    fun parseIsoDate(dueDate: String): Calendar? {
        return try {
            val d = iso.parse(dueDate) ?: return null
            Calendar.getInstance().apply {
                time = d
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun formatIso(cal: Calendar): String = iso.format(cal.time)

    fun formatTriggerLocal(millis: Long): String = localStamp.format(Date(millis))

    /** Dia do aviso: dueDate − 1 dia (day_before) ou dueDate (due_date). */
    fun alarmTargetDate(dueDate: String, kind: String): String? {
        val due = parseIsoDate(dueDate) ?: return null
        return when (kind) {
            InvoiceReminderPrefs.KIND_DAY_BEFORE -> {
                due.add(Calendar.DAY_OF_YEAR, -1)
                formatIso(due)
            }
            InvoiceReminderPrefs.KIND_DUE_DATE -> dueDate
            else -> null
        }
    }

    /**
     * @return triggerAtMillis (RTC) ou null se já passou de 13:00 do dia-alvo.
     *
     * - antes de 12:00 → 12:00
     * - entre 12:00 e 13:00 → agora + [SOON_DELAY_MS] (se ainda < 13:00)
     * - após 13:00 → null
     */
    fun computeTriggerAt(
        targetDateIso: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val day = parseIsoDate(targetDateIso) ?: return null
        val noon = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, TRIGGER_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val cutoff = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, CUTOFF_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (nowMillis >= cutoff) return null
        if (nowMillis < noon) return noon

        val soon = nowMillis + SOON_DELAY_MS
        if (soon >= cutoff) return null
        return soon
    }

    fun isOpenInvoice(invoice: InvoiceEntity): Boolean = !isTerminalInvoice(invoice)

    fun isTerminalInvoice(invoice: InvoiceEntity): Boolean {
        val group = invoice.sourceGroup.orEmpty().lowercase(Locale.ROOT)
        if (group == InvoiceEntity.GROUP_PAGAS || group == InvoiceEntity.GROUP_CANCELADAS) {
            return true
        }
        val st = invoice.status.uppercase(Locale.ROOT)
        if (st in setOf("P", "C", "R")) return true
        val text = invoice.statusText.orEmpty().lowercase(Locale.ROOT)
        return text.contains("paga") || text.contains("pago") || text.contains("cancel")
    }
}
