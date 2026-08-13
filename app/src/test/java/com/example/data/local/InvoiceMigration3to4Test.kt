package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InvoiceMigration3to4Test {

    private val dbName = "migration_3_4_test.db"
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
    fun migration3to4_preservesNotificationsAndReceipts_andCreatesInvoices() = runBlocking {
        createV3Database()

        val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()

        try {
            val cursor = roomDb.openHelper.readableDatabase.query("SELECT COUNT(*) FROM notifications")
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
            cursor.close()

            val receiptCursor =
                roomDb.openHelper.readableDatabase.query("SELECT COUNT(*) FROM receipt_history")
            receiptCursor.moveToFirst()
            assertEquals(1, receiptCursor.getInt(0))
            receiptCursor.close()

            val dao = roomDb.invoiceDao()
            assertEquals(0, dao.count())

            dao.upsert(
                InvoiceEntity(
                    idReceber = "749428",
                    idContrato = "12759",
                    amountCents = 8000,
                    amountOpenCents = 8000,
                    dueDate = "2026-08-17",
                    status = "A",
                    statusText = "Em aberto",
                    billingType = InvoiceEntity.BILLING_BANK,
                    rawBillingType = "Boleto",
                    barcode = null,
                    sourceGroup = InvoiceEntity.GROUP_ABERTAS
                )
            )
            assertEquals(1, dao.count())
            assertNotNull(dao.findByIdReceber("749428"))

            dao.upsert(
                InvoiceEntity(
                    idReceber = "749428",
                    idContrato = "12759",
                    amountCents = 9000,
                    amountOpenCents = 9000,
                    dueDate = "2026-08-20",
                    status = "A",
                    statusText = "Em aberto",
                    billingType = InvoiceEntity.BILLING_BANK,
                    rawBillingType = "Boleto",
                    barcode = null,
                    sourceGroup = InvoiceEntity.GROUP_ABERTAS
                )
            )
            assertEquals("upsert não deve duplicar", 1, dao.count())
            assertEquals(9000L, dao.findByIdReceber("749428")!!.amountCents)
            assertEquals("2026-08-20", dao.findByIdReceber("749428")!!.dueDate)
            assertTrue(roomDb.openHelper.readableDatabase.version >= 4)
        } finally {
            roomDb.close()
        }
    }

    private fun createV3Database() {
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `notifications` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `body` TEXT NOT NULL,
                            `timestamp` INTEGER NOT NULL,
                            `isRead` INTEGER NOT NULL,
                            `type` TEXT NOT NULL,
                            `messageId` TEXT,
                            `contentHash` TEXT NOT NULL,
                            `targetUrl` TEXT
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_notifications_messageId` " +
                            "ON `notifications` (`messageId`)"
                    )
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
                "INSERT INTO notifications (title, body, timestamp, isRead, type, messageId, contentHash, targetUrl) " +
                    "VALUES ('Aviso', 'Corpo', 1000, 0, 'general', 'm1', 'h1', NULL)"
            )
            db.execSQL(
                "INSERT INTO receipt_history (localFilePath, mimeType, originalFileName, clientName, " +
                    "clientCode, clientContract, createdAt, sentAt, status) " +
                    "VALUES ('/tmp/a.jpg', 'image/jpeg', 'a.jpg', 'Cliente', '19', '12759', 2000, NULL, 'prepared')"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table " +
                    "(id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                    "VALUES (42, 'v3_placeholder_hash')"
            )
        } finally {
            db.close()
            helper.close()
        }
    }
}
