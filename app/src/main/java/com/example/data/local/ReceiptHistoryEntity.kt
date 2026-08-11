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
    /** Sem confirmação confiável do WhatsApp; permanece null. */
    val sentAt: Long? = null,
    /** shared = compartilhamento iniciado pelo app. */
    val status: String = STATUS_SHARED
) {
    companion object {
        const val STATUS_SHARED = "shared"
    }
}
