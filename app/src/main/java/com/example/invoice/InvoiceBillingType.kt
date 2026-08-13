package com.example.invoice

import com.example.data.local.InvoiceEntity

/**
 * Mapeia tipo_recebimento IXC → STORE | BANK | UNKNOWN.
 * PIX (se aparecer) permanece UNKNOWN — não confirma pagamento.
 */
object InvoiceBillingType {
    fun fromRaw(raw: String?): String {
        val key = raw?.trim().orEmpty()
        if (key.isEmpty()) return InvoiceEntity.BILLING_UNKNOWN
        return when (key.lowercase()) {
            "boleto",
            "gateway",
            "arrecadacaorecebimento",
            "arrecadaçãorecebimento" -> InvoiceEntity.BILLING_BANK
            "fatura" -> InvoiceEntity.BILLING_STORE
            else -> InvoiceEntity.BILLING_UNKNOWN
        }
    }
}
