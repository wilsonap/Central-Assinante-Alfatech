package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adiciona tabela de histórico local de comprovantes sem apagar notifications.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `receipt_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `localFilePath` TEXT NOT NULL,
                `mimeType` TEXT NOT NULL,
                `originalFileName` TEXT,
                `clientName` TEXT NOT NULL,
                `clientCode` TEXT,
                `clientContract` TEXT,
                `createdAt` INTEGER NOT NULL,
                `sentAt` INTEGER,
                `status` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
