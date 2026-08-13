package com.example.invoice

import android.util.Log
import com.example.data.local.InvoiceEntity
import java.math.BigDecimal
import java.math.RoundingMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * Extrai faturas do JSON getFaturas sem persistir PII nem o JSON completo.
 */
object InvoiceParser {

    private const val TAG = "INVOICE_SYNC"

    data class ParseResult(
        val invoices: List<InvoiceEntity>,
        val skipped: Int
    )

    fun parseGetFaturasJson(raw: String, now: Long = System.currentTimeMillis()): ParseResult {
        if (raw.isBlank()) return ParseResult(emptyList(), 0)
        return try {
            val root = JSONObject(raw)
            val byId = linkedMapOf<String, InvoiceEntity>()
            var skipped = 0

            fun accept(item: JSONObject, group: String?) {
                val parsed = parseOne(item, group, now)
                if (parsed == null) {
                    skipped++
                    return
                }
                val prev = byId[parsed.idReceber]
                // Preferir grupo mais específico se já existir; senão sobrescreve com sync atual.
                byId[parsed.idReceber] = if (prev == null) {
                    parsed
                } else {
                    parsed.copy(
                        createdAt = prev.createdAt,
                        sourceGroup = parsed.sourceGroup ?: prev.sourceGroup
                    )
                }
            }

            // Grupos top-level confirmados no HAR.
            listOf(
                InvoiceEntity.GROUP_VENCIDAS,
                InvoiceEntity.GROUP_PENDENTES,
                InvoiceEntity.GROUP_ABERTAS,
                InvoiceEntity.GROUP_PAGAS,
                InvoiceEntity.GROUP_CANCELADAS
            ).forEach { group ->
                collectFromNode(root.opt(group), group, ::accept)
            }

            // Nested: faturas[] com fatura[] por contrato, ou array plano.
            collectFromNode(root.opt("faturas"), group = null, accept = ::accept)

            Log.i(TAG, "parse invoiceCount=${byId.size} skipped=$skipped syncSuccess=true")
            ParseResult(byId.values.toList(), skipped)
        } catch (e: Exception) {
            Log.w(TAG, "parse fail=${e.javaClass.simpleName} syncSuccess=false")
            ParseResult(emptyList(), 0)
        }
    }

    private fun collectFromNode(
        node: Any?,
        group: String?,
        accept: (JSONObject, String?) -> Unit
    ) {
        when (node) {
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    val item = node.opt(i) ?: continue
                    when (item) {
                        is JSONObject -> {
                            val nested = item.opt("fatura")
                            if (nested is JSONArray || nested is JSONObject) {
                                collectFromNode(nested, group, accept)
                            } else if (looksLikeInvoice(item)) {
                                accept(item, group)
                            } else {
                                // Contrato com outros campos: varrer arrays internas.
                                val keys = item.keys()
                                while (keys.hasNext()) {
                                    val k = keys.next()
                                    val v = item.opt(k)
                                    if (v is JSONArray) collectFromNode(v, group, accept)
                                }
                            }
                        }
                    }
                }
            }
            is JSONObject -> {
                if (looksLikeInvoice(node)) {
                    accept(node, group)
                } else {
                    val keys = node.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = node.opt(k)
                        if (v is JSONArray || v is JSONObject) {
                            val inferredGroup = when (k.lowercase()) {
                                "vencidas" -> InvoiceEntity.GROUP_VENCIDAS
                                "pendentes" -> InvoiceEntity.GROUP_PENDENTES
                                "abertas" -> InvoiceEntity.GROUP_ABERTAS
                                "pagas" -> InvoiceEntity.GROUP_PAGAS
                                "canceladas" -> InvoiceEntity.GROUP_CANCELADAS
                                else -> group
                            }
                            collectFromNode(v, inferredGroup, accept)
                        }
                    }
                }
            }
        }
    }

    private fun looksLikeInvoice(o: JSONObject): Boolean {
        return o.has("id") || o.has("data_vencimento") || o.has("valor") ||
            o.has("tipo_recebimento") || o.has("id_receber")
    }

    private fun parseOne(item: JSONObject, group: String?, now: Long): InvoiceEntity? {
        val idRaw = firstNonBlank(item, "id", "id_receber", "ID_RECEBER") ?: run {
            Log.w(TAG, "skip missing id")
            return null
        }
        val idReceber = idRaw.trim()
        if (idReceber.isEmpty()) {
            Log.w(TAG, "skip blank id")
            return null
        }

        val amountCents = parseAmountToCents(firstNonBlank(item, "valor", "valor_total", "valorTotal"))
        if (amountCents == null) {
            Log.w(TAG, "skip invalid amount idReceber=${maskId(idReceber)}")
            return null
        }

        val dueDate = normalizeDueDate(
            firstNonBlank(item, "data_vencimento", "dataVencimento", "vencimento")
        )
        if (dueDate == null) {
            Log.w(TAG, "skip invalid dueDate idReceber=${maskId(idReceber)}")
            return null
        }

        val status = firstNonBlank(item, "status", "status_recebimento")?.trim().orEmpty()
        if (status.isEmpty()) {
            Log.w(TAG, "skip blank status idReceber=${maskId(idReceber)}")
            return null
        }

        val amountOpen = parseAmountToCents(
            firstNonBlank(item, "valor_aberto", "valorAberto")
        )
        val rawBilling = firstNonBlank(item, "tipo_recebimento", "tipoRecebimento")
        val barcodeRaw = firstNonBlank(item, "linha_digitavel", "linhaDigitavel", "codigo_barras")
        val barcode = barcodeRaw?.trim()?.takeIf { it.isNotEmpty() }

        val inferredGroup = group ?: inferGroupFromStatusText(
            firstNonBlank(item, "status_text", "statusText")
        )

        return InvoiceEntity(
            idReceber = idReceber,
            idContrato = firstNonBlank(item, "id_contrato", "idContrato"),
            amountCents = amountCents,
            amountOpenCents = amountOpen,
            dueDate = dueDate,
            status = status,
            statusText = firstNonBlank(item, "status_text", "statusText"),
            billingType = InvoiceBillingType.fromRaw(rawBilling),
            rawBillingType = rawBilling,
            barcode = barcode,
            sourceGroup = inferredGroup,
            lastSyncedAt = now,
            lastSeenAt = now,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun inferGroupFromStatusText(statusText: String?): String? {
        val t = statusText?.lowercase().orEmpty()
        return when {
            t.contains("cancel") -> InvoiceEntity.GROUP_CANCELADAS
            t.contains("paga") || t.contains("pago") || t.contains("quitad") ->
                InvoiceEntity.GROUP_PAGAS
            t.contains("vencid") -> InvoiceEntity.GROUP_VENCIDAS
            t.contains("abert") || t.contains("pendente") -> InvoiceEntity.GROUP_ABERTAS
            else -> null
        }
    }

    fun parseAmountToCents(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        return try {
            val normalized = raw.trim()
                .replace("R$", "", ignoreCase = true)
                .replace(" ", "")
                .replace(",", ".")
            val bd = BigDecimal(normalized)
            bd.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    /** Aceita yyyy-MM-dd ou dd/MM/yyyy. */
    fun normalizeDueDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim().take(10)
        if (Regex("""\d{4}-\d{2}-\d{2}""").matches(t)) return t
        val br = Regex("""(\d{2})/(\d{2})/(\d{4})""").matchEntire(t) ?: return null
        return "${br.groupValues[3]}-${br.groupValues[2]}-${br.groupValues[1]}"
    }

    fun maskId(idReceber: String): String {
        if (idReceber.length <= 3) return "***"
        return "***${idReceber.takeLast(3)}"
    }

    private fun firstNonBlank(obj: JSONObject, vararg keys: String): String? {
        for (key in keys) {
            if (!obj.has(key) || obj.isNull(key)) continue
            val v = obj.opt(key) ?: continue
            val s = v.toString().trim()
            if (s.isNotEmpty() && s != "null") return s
        }
        return null
    }
}
