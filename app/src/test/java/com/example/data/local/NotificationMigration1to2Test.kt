package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationMigration1to2Test {

    private val dbName = "migration_1_2_test.db"
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
        AppDatabase.clearInstanceForTests()
    }

    @After
    fun tearDown() {
        AppDatabase.clearInstanceForTests()
        context.deleteDatabase(dbName)
    }

    @Test
    fun migration1to2_preservesRows_andAllowsNewInserts_andUniqueMessageId() = runBlocking {
        createV1DatabaseWithTwoNotifications()

        // Aplica Migration(1,2) e abre Room na versão 2
        val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = roomDb.notificationDao()
            val rows = dao.getAllNotifications().first()

            assertEquals("mensagens v1 devem permanecer", 2, rows.size)
            assertTrue(rows.any { it.title == "Aviso Antigo 1" && it.body == "Corpo 1" })
            assertTrue(rows.any { it.title == "Aviso Antigo 2" && it.body == "Corpo 2" })

            rows.forEach { row ->
                assertNull("messageId antigo deve ser NULL", row.messageId)
                assertNull("targetUrl antigo deve ser NULL", row.targetUrl)
                assertTrue(
                    "contentHash legado seguro: ${row.contentHash}",
                    row.contentHash.startsWith("legacy_")
                )
            }

            // Nova inserção com messageId
            val inserted = dao.insertNotification(
                NotificationEntity(
                    title = "Nova",
                    body = "Mensagem nova",
                    type = "general",
                    messageId = "msg-unique-1",
                    contentHash = "hash-nova",
                    targetUrl = "https://example.com"
                )
            )
            assertTrue("insert novo deve funcionar", inserted > 0)

            val afterInsert = dao.getAllNotifications().first()
            assertEquals(3, afterInsert.size)
            assertNotNull(dao.findByMessageId("msg-unique-1"))

            // Índice UNIQUE em messageId: segundo insert com mesmo id é ignorado
            val duplicate = dao.insertNotification(
                NotificationEntity(
                    title = "Duplicata",
                    body = "Não deve entrar",
                    type = "general",
                    messageId = "msg-unique-1",
                    contentHash = "hash-dup",
                    targetUrl = null
                )
            )
            assertEquals("conflito unique → IGNORE", -1L, duplicate)
            assertEquals(3, dao.getAllNotifications().first().size)
        } finally {
            roomDb.close()
        }
    }

    private fun createV1DatabaseWithTwoNotifications() {
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `notifications` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `body` TEXT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `isRead` INTEGER NOT NULL,
                            `type` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) = Unit
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        try {
            db.execSQL(
                "INSERT INTO notifications (title, body, timestamp, isRead, type) " +
                    "VALUES ('Aviso Antigo 1', 'Corpo 1', 1000, 0, 'general')"
            )
            db.execSQL(
                "INSERT INTO notifications (title, body, timestamp, isRead, type) " +
                    "VALUES ('Aviso Antigo 2', 'Corpo 2', 2000, 1, 'general')"
            )
            // room_master_table na versão 1 para Room reconhecer upgrade
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table " +
                    "(id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                    "VALUES (42, 'v1_placeholder_hash')"
            )
        } finally {
            db.close()
            helper.close()
        }
    }
}
