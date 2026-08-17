package com.example.invoice

import com.example.data.local.InvoiceEntity
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Status visual do card de fatura (somente UI).
 * Respeita o mapeamento IXC já usado em [InvoiceAlarmTiming] / DAO.
 */
enum class InvoiceVisualKind {
    PAID,
    CANCELLED,
    OPEN_FUTURE,
    DUE_TOMORROW,
    DUE_TODAY,
    OVERDUE,
    FALLBACK
}

data class InvoiceDisplayStatus(
    val label: String,
    val kind: InvoiceVisualKind,
    /** Dias restantes (futuro) ou dias de atraso (vencida); null se N/A. */
    val days: Int? = null
)

object InvoiceDisplayStatusMapper {

    private val TZ: TimeZone = TimeZone.getTimeZone("America/Sao_Paulo")

    fun todayIso(): String = formatIso(Calendar.getInstance(TZ))

    fun getInvoiceDisplayStatus(
        invoice: InvoiceEntity,
        todayIso: String = todayIso()
    ): InvoiceDisplayStatus {
        if (isPaid(invoice)) {
            return InvoiceDisplayStatus(label = "Paga", kind = InvoiceVisualKind.PAID)
        }
        if (isCancelled(invoice)) {
            return InvoiceDisplayStatus(label = "Cancelada", kind = InvoiceVisualKind.CANCELLED)
        }

        val due = invoice.dueDate.trim().take(10)
        val daysUntil = daysFromTo(todayIso, due)
        if (daysUntil == null) {
            return fallbackOpen(invoice)
        }

        return when {
            daysUntil > 1 -> InvoiceDisplayStatus(
                label = "Vence em $daysUntil dias",
                kind = InvoiceVisualKind.OPEN_FUTURE,
                days = daysUntil
            )
            daysUntil == 1 -> InvoiceDisplayStatus(
                label = "Vence amanhã",
                kind = InvoiceVisualKind.DUE_TOMORROW,
                days = 1
            )
            daysUntil == 0 -> InvoiceDisplayStatus(
                label = "Vence hoje",
                kind = InvoiceVisualKind.DUE_TODAY,
                days = 0
            )
            else -> {
                val overdue = -daysUntil
                val label = if (overdue == 1) {
                    "Vencida há 1 dia"
                } else {
                    "Vencida há $overdue dias"
                }
                InvoiceDisplayStatus(
                    label = label,
                    kind = InvoiceVisualKind.OVERDUE,
                    days = overdue
                )
            }
        }
    }

    /**
     * Paga / recebida / baixada — mapeamento já usado no app:
     * grupo `pagas`, status `P` ou `R`, ou statusText com paga/pago/quitad.
     */
    fun isPaid(invoice: InvoiceEntity): Boolean {
        val group = invoice.sourceGroup.orEmpty().lowercase(Locale.ROOT)
        if (group == InvoiceEntity.GROUP_PAGAS) return true
        val st = invoice.status.trim().uppercase(Locale.ROOT)
        if (st == "P" || st == "R") return true
        val text = invoice.statusText.orEmpty().lowercase(Locale.ROOT)
        return text.contains("paga") || text.contains("pago") || text.contains("quitad") ||
            text.contains("recebid") || text.contains("baixad")
    }

    /** Cancelada — grupo `canceladas`, status `C`, ou statusText com cancel. */
    fun isCancelled(invoice: InvoiceEntity): Boolean {
        val group = invoice.sourceGroup.orEmpty().lowercase(Locale.ROOT)
        if (group == InvoiceEntity.GROUP_CANCELADAS) return true
        val st = invoice.status.trim().uppercase(Locale.ROOT)
        if (st == "C") return true
        return invoice.statusText.orEmpty().lowercase(Locale.ROOT).contains("cancel")
    }

    private fun fallbackOpen(invoice: InvoiceEntity): InvoiceDisplayStatus {
        val text = invoice.statusText?.trim().orEmpty()
        val label = when {
            text.isNotEmpty() -> text.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("pt-BR")) else it.toString()
            }
            else -> "Em aberto"
        }
        return InvoiceDisplayStatus(label = label, kind = InvoiceVisualKind.FALLBACK)
    }

    /** Dias de [fromIso] até [toIso] (to - from). Null se data inválida. */
    fun daysFromTo(fromIso: String, toIso: String): Int? {
        val from = parseIsoDay(fromIso) ?: return null
        val to = parseIsoDay(toIso) ?: return null
        val diffMs = to.timeInMillis - from.timeInMillis
        return (diffMs / 86_400_000L).toInt()
    }

    private fun parseIsoDay(iso: String): Calendar? {
        val t = iso.trim().take(10)
        val parts = t.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        if (m !in 1..12 || d !in 1..31) return null
        return Calendar.getInstance(TZ).apply {
            clear()
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, d)
        }
    }

    private fun formatIso(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }
}
