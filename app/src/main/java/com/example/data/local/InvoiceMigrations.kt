package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adiciona tabela local de faturas sem apagar notifications nem receipt_history.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `invoices` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `idReceber` TEXT NOT NULL,
                `idContrato` TEXT,
                `amountCents` INTEGER NOT NULL,
                `amountOpenCents` INTEGER,
                `dueDate` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `statusText` TEXT,
                `billingType` TEXT NOT NULL,
                `rawBillingType` TEXT,
                `barcode` TEXT,
                `sourceGroup` TEXT,
                `lastSyncedAt` INTEGER NOT NULL,
                `lastSeenAt` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_invoices_idReceber` ON `invoices` (`idReceber`)"
        )
    }
}
