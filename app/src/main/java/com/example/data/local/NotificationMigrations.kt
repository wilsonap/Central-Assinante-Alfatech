package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrações Room do [AppDatabase].
 *
 * SQL exato da Migration(1, 2):
 *
 * ```
 * ALTER TABLE notifications ADD COLUMN messageId TEXT;
 * ALTER TABLE notifications ADD COLUMN contentHash TEXT NOT NULL DEFAULT '';
 * ALTER TABLE notifications ADD COLUMN targetUrl TEXT;
 * UPDATE notifications SET contentHash = 'legacy_' || id WHERE contentHash = '';
 * CREATE UNIQUE INDEX IF NOT EXISTS `index_notifications_messageId` ON `notifications` (`messageId`);
 * ```
 *
 * Registros antigos: messageId/targetUrl ficam NULL; contentHash vira `legacy_<id>`
 * (único por linha, evita colisão no dedupe por hash). Índice UNIQUE permite múltiplos NULL.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notifications ADD COLUMN messageId TEXT")
        db.execSQL(
            "ALTER TABLE notifications ADD COLUMN contentHash TEXT NOT NULL DEFAULT ''"
        )
        db.execSQL("ALTER TABLE notifications ADD COLUMN targetUrl TEXT")
        db.execSQL(
            "UPDATE notifications SET contentHash = 'legacy_' || id WHERE contentHash = ''"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_notifications_messageId` " +
                "ON `notifications` (`messageId`)"
        )
    }
}
