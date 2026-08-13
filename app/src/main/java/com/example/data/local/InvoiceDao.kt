package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY dueDate ASC, idReceber ASC")
    fun observeAll(): Flow<List<InvoiceEntity>>

    @Query(
        """
        SELECT * FROM invoices
        WHERE sourceGroup IN ('abertas', 'vencidas', 'pendentes')
           OR (status NOT IN ('P', 'C', 'R') AND IFNULL(sourceGroup, '') NOT IN ('pagas', 'canceladas'))
        ORDER BY dueDate ASC
        """
    )
    fun observeOpen(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY dueDate ASC")
    suspend fun getAll(): List<InvoiceEntity>

    @Query("SELECT * FROM invoices WHERE idReceber = :idReceber LIMIT 1")
    suspend fun findByIdReceber(idReceber: String): InvoiceEntity?

    @Query(
        """
        SELECT * FROM invoices
        WHERE dueDate = :dueDate
          AND IFNULL(sourceGroup, '') NOT IN ('pagas', 'canceladas')
          AND status NOT IN ('P', 'C', 'R')
        """
    )
    suspend fun findOpenByDueDate(dueDate: String): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: InvoiceEntity): Long

    @Query(
        """
        UPDATE invoices SET
            idContrato = :idContrato,
            amountCents = :amountCents,
            amountOpenCents = :amountOpenCents,
            dueDate = :dueDate,
            status = :status,
            statusText = :statusText,
            billingType = :billingType,
            rawBillingType = :rawBillingType,
            barcode = :barcode,
            sourceGroup = :sourceGroup,
            lastSyncedAt = :now,
            lastSeenAt = :now,
            updatedAt = :now
        WHERE idReceber = :idReceber
        """
    )
    suspend fun updateByIdReceber(
        idReceber: String,
        idContrato: String?,
        amountCents: Long,
        amountOpenCents: Long?,
        dueDate: String,
        status: String,
        statusText: String?,
        billingType: String,
        rawBillingType: String?,
        barcode: String?,
        sourceGroup: String?,
        now: Long
    ): Int

    @Transaction
    suspend fun upsert(entity: InvoiceEntity) {
        val existing = findByIdReceber(entity.idReceber)
        if (existing == null) {
            insertIgnore(entity)
        } else {
            updateByIdReceber(
                idReceber = entity.idReceber,
                idContrato = entity.idContrato,
                amountCents = entity.amountCents,
                amountOpenCents = entity.amountOpenCents,
                dueDate = entity.dueDate,
                status = entity.status,
                statusText = entity.statusText,
                billingType = entity.billingType,
                rawBillingType = entity.rawBillingType,
                barcode = entity.barcode,
                sourceGroup = entity.sourceGroup,
                now = entity.lastSyncedAt
            )
        }
    }

    @Query("SELECT MAX(lastSyncedAt) FROM invoices")
    suspend fun latestSyncAt(): Long?

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int
}
