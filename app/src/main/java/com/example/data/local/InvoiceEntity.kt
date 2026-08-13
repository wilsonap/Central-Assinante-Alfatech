package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["idReceber"], unique = true)]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Chave de negócio IXC = fatura.id */
    val idReceber: String,
    val idContrato: String? = null,
    /** Valor em centavos. */
    val amountCents: Long,
    val amountOpenCents: Long? = null,
    /** Data de vencimento ISO yyyy-MM-dd. */
    val dueDate: String,
    /** Status original IXC (ex.: A). */
    val status: String,
    val statusText: String? = null,
    /** STORE | BANK | UNKNOWN */
    val billingType: String,
    /** tipo_recebimento original. */
    val rawBillingType: String? = null,
    /** linha_digitavel — opcional. */
    val barcode: String? = null,
    /** Grupo da resposta IXC: abertas, vencidas, pagas, canceladas, pendentes. */
    val sourceGroup: String? = null,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val BILLING_STORE = "STORE"
        const val BILLING_BANK = "BANK"
        const val BILLING_UNKNOWN = "UNKNOWN"

        const val GROUP_ABERTAS = "abertas"
        const val GROUP_VENCIDAS = "vencidas"
        const val GROUP_PENDENTES = "pendentes"
        const val GROUP_PAGAS = "pagas"
        const val GROUP_CANCELADAS = "canceladas"
    }
}
