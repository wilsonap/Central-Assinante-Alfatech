package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptHistoryDao {
    @Query("SELECT * FROM receipt_history ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ReceiptHistoryEntity>>

    @Query("SELECT * FROM receipt_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReceiptHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReceiptHistoryEntity): Long

    @Query("DELETE FROM receipt_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM receipt_history")
    suspend fun count(): Int
}
