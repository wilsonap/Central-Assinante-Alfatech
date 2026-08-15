package com.example.invoice

import com.example.data.local.InvoiceEntity
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

/**
 * Janela de lembrete: 11:00–13:00 no dia-alvo (não exato).
 */
object InvoiceAlarmTiming {

    const val WINDOW_START_HOUR = 11
    const val WINDOW_END_HOUR = 13
    const val WINDOW_LENGTH_MS = 2L * 60L * 60L * 1000L

    private val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

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
     * @return Pair(windowStartMillis, windowLengthMillis) ou null se já passou de 13:00 do dia-alvo.
     */
    fun computeWindow(
        targetDateIso: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<Long, Long>? {
        val day = parseIsoDate(targetDateIso) ?: return null
        val windowStart = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, WINDOW_START_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val windowEnd = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, WINDOW_END_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (nowMillis >= windowEnd) return null

        val start = maxOf(windowStart, nowMillis)
        val length = windowEnd - start
        if (length <= 0L) return null
        return start to length
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
