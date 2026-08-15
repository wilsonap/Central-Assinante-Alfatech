package com.example.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.invoice.InvoiceReminderPrefs
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InvoiceReminderHistoryPersistTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        AppDatabase.clearInstanceForTests()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val field = AppDatabase::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, db)
    }

    @After
    fun tearDown() {
        AppDatabase.clearInstanceForTests()
        db.close()
    }

    @Test
    fun dayBefore_persistsOnce_withInvoiceType() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val key = InvoiceReminderPrefs.notificationKey(
            "749428",
            InvoiceReminderPrefs.KIND_DAY_BEFORE,
            "2026-09-20"
        )
        assertTrue(
            PushNotificationRepository.persistInvoiceReminder(
                context,
                key,
                InvoiceReminderPrefs.KIND_DAY_BEFORE
            )
        )
        assertFalse(
            PushNotificationRepository.persistInvoiceReminder(
                context,
                key,
                InvoiceReminderPrefs.KIND_DAY_BEFORE
            )
        )
        val all = db.notificationDao().findByMessageId(key)
        assertEquals(PushNotificationRepository.TYPE_INVOICE_REMINDER, all?.type)
        assertEquals("Fatura vence amanhã", all?.title)
        assertFalse(all?.body?.contains("R$") == true)
    }

    @Test
    fun dueDate_and_fcm_coexist() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertTrue(
            PushNotificationRepository.persistFromPush(
                context,
                title = "Comunicado",
                body = "Teste FCM",
                type = "general",
                messageId = "fcm-1",
                targetUrl = null
            )
        )
        val key = InvoiceReminderPrefs.notificationKey(
            "1",
            InvoiceReminderPrefs.KIND_DUE_DATE,
            "2026-09-20"
        )
        assertTrue(
            PushNotificationRepository.persistInvoiceReminder(
                context,
                key,
                InvoiceReminderPrefs.KIND_DUE_DATE
            )
        )
        assertEquals("Fatura vence hoje", db.notificationDao().findByMessageId(key)?.title)
        assertEquals("Comunicado", db.notificationDao().findByMessageId("fcm-1")?.title)
    }

    @Test
    fun firedTrue_historyMissing_backfillInsertsWithoutDuplicate() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val key = InvoiceReminderPrefs.notificationKey(
            "749428",
            InvoiceReminderPrefs.KIND_DAY_BEFORE,
            "2026-08-16"
        )
        InvoiceReminderPrefs.markFired(context, key)
        assertTrue(InvoiceReminderPrefs.wasFired(context, key))
        assertEquals(null, db.notificationDao().findByMessageId(key))

        assertTrue(
            PushNotificationRepository.persistInvoiceReminder(
                context,
                key,
                InvoiceReminderPrefs.KIND_DAY_BEFORE
            )
        )
        assertEquals("Fatura vence amanhã", db.notificationDao().findByMessageId(key)?.title)
        assertFalse(
            PushNotificationRepository.persistInvoiceReminder(
                context,
                key,
                InvoiceReminderPrefs.KIND_DAY_BEFORE
            )
        )
        assertTrue(InvoiceReminderPrefs.wasFired(context, key))
    }
}
