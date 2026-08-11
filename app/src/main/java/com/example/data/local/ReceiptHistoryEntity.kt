package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipt_history")
data class ReceiptHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Caminho absoluto do arquivo em filesDir/receipts (privado). */
    val localFilePath: String,
    val mimeType: String,
    val originalFileName: String? = null,
    val clientName: String,
    val clientCode: String? = null,
    val clientContract: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /**
     * Sem confirmação confiável do WhatsApp (ACTION_SEND não informa se o usuário tocou Enviar).
     * Permanece null — não usar como prova de envio.
     */
    val sentAt: Long? = null,
    /**
     * prepared = cópia salva no histórico local.
     * shared = startActivity do compartilhamento executado com sucesso (não confirma envio no WhatsApp).
     * Nunca usar "sent" como confirmação de envio.
     */
    val status: String = STATUS_PREPARED
) {
    companion object {
        const val STATUS_PREPARED = "prepared"
        const val STATUS_SHARED = "shared"
    }
}
