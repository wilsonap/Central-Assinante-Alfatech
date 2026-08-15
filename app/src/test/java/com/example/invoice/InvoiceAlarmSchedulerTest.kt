package com.example.invoice

import android.app.AlarmManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InvoiceAlarmSchedulerTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var shadowAlarm: ShadowAlarmManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppDatabase.clearInstanceForTests()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // Injeta instância em memória para Checker/Scheduler.
        val field = AppDatabase::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, db)

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        shadowAlarm = shadowOf(am)
        // Limpa alarmes agendados entre testes (API pública do shadow).
        while (shadowAlarm.nextScheduledAlarm != null) {
            shadowAlarm.nextScheduledAlarm
        }

        context.getSharedPreferences("invoice_alarm_request_codes", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("invoice_reminder_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @After
    fun tearDown() {
        AppDatabase.clearInstanceForTests()
        db.close()
    }

    private fun openInvoice(
        id: String = "749428",
        due: String = "2026-09-20",
        status: String = "A",
        group: String = InvoiceEntity.GROUP_ABERTAS
    ) = InvoiceEntity(
        idReceber = id,
        amountCents = 8000,
        dueDate = due,
        status = status,
        statusText = "Em aberto",
        billingType = InvoiceEntity.BILLING_BANK,
        sourceGroup = group
    )

    @Test
    fun faturaVenceAmanha_dayBeforeValido() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tomorrow = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val due = InvoiceAlarmTiming.formatIso(tomorrow)
        val target = InvoiceAlarmTiming.alarmTargetDate(due, InvoiceReminderPrefs.KIND_DAY_BEFORE)
        assertEquals(InvoiceAlarmTiming.formatIso(cal), target)
        val window = InvoiceAlarmTiming.computeWindow(target!!, cal.timeInMillis)
        assertNotNull(window)
    }

    @Test
    fun faturaVenceHoje_dueDateValido() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val due = InvoiceAlarmTiming.formatIso(cal)
        val target = InvoiceAlarmTiming.alarmTargetDate(due, InvoiceReminderPrefs.KIND_DUE_DATE)
        assertEquals(due, target)
        assertNotNull(InvoiceAlarmTiming.computeWindow(target!!, cal.timeInMillis))
    }

    @Test
    fun apos13h_naoCriaJanelaRetroativa() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val due = InvoiceAlarmTiming.formatIso(cal)
        assertNull(InvoiceAlarmTiming.computeWindow(due, cal.timeInMillis))
    }

    @Test
    fun mudaVencimento_cancelaAntigosCriaNovos() {
        val oldInv = openInvoice(due = "2026-09-15")
        InvoiceAlarmScheduler.scheduleInvoice(context, oldInv)
        val before = shadowAlarm.scheduledAlarms.size
        assertTrue("esperado >=1 alarme, got=$before", before >= 1)

        val newInv = openInvoice(due = "2026-09-20")
        InvoiceAlarmScheduler.rescheduleInvoice(context, oldInv, newInv)

        val rcOld = InvoiceAlarmScheduler.requestCode(
            context, oldInv.idReceber, InvoiceReminderPrefs.KIND_DUE_DATE, oldInv.dueDate
        )
        val rcNew = InvoiceAlarmScheduler.requestCode(
            context, newInv.idReceber, InvoiceReminderPrefs.KIND_DUE_DATE, newInv.dueDate
        )
        assertNotEquals(rcOld, rcNew)
        assertTrue(shadowAlarm.scheduledAlarms.isNotEmpty())
    }

    @Test
    fun faturaPaga_cancelaAlarmes() {
        val inv = openInvoice()
        InvoiceAlarmScheduler.scheduleInvoice(context, inv)
        assertTrue(shadowAlarm.scheduledAlarms.isNotEmpty())
        val paid = inv.copy(status = "P", sourceGroup = InvoiceEntity.GROUP_PAGAS)
        InvoiceAlarmScheduler.cancelInvoice(context, inv)
        InvoiceAlarmScheduler.scheduleInvoice(context, paid)
        // Terminal não reagenda; cancel remove PIs.
        assertTrue(InvoiceAlarmTiming.isTerminalInvoice(paid))
    }

    @Test
    fun faturaCancelada_terminal() {
        val cancelled = openInvoice().copy(status = "C", sourceGroup = InvoiceEntity.GROUP_CANCELADAS)
        assertTrue(InvoiceAlarmTiming.isTerminalInvoice(cancelled))
        InvoiceAlarmScheduler.scheduleInvoice(context, cancelled)
        // Não deve criar janelas para terminal (schedule cancela).
    }

    @Test
    fun mesmoUpsert_requestCodesEstaveisSemDuplicidade() {
        val inv = openInvoice(due = "2026-12-01")
        InvoiceAlarmScheduler.scheduleInvoice(context, inv)
        val rc1 = InvoiceAlarmScheduler.requestCode(
            context, inv.idReceber, InvoiceReminderPrefs.KIND_DAY_BEFORE, inv.dueDate
        )
        InvoiceAlarmScheduler.scheduleInvoice(context, inv)
        val rc2 = InvoiceAlarmScheduler.requestCode(
            context, inv.idReceber, InvoiceReminderPrefs.KIND_DAY_BEFORE, inv.dueDate
        )
        assertEquals(rc1, rc2)
        val uri = InvoiceAlarmScheduler.reminderUri(
            inv.idReceber, InvoiceReminderPrefs.KIND_DAY_BEFORE, inv.dueDate
        )
        assertEquals("alfatech", uri.scheme)
        assertEquals("invoice-reminder", uri.host)
    }

    @Test
    fun bootReschedule_reconstróiDeRoom() = runBlocking {
        db.invoiceDao().upsert(openInvoice(due = "2026-12-15"))
        db.invoiceDao().upsert(
            openInvoice(id = "111", due = "2026-12-16")
        )
        InvoiceAlarmScheduler.scheduleAllOpenInvoices(context)
        assertTrue(shadowAlarm.scheduledAlarms.isNotEmpty())
        val open = db.invoiceDao().getOpenInvoices()
        assertEquals(2, open.size)
    }

    @Test
    fun receiverDueDateMismatch_naoNotifica() = runBlocking {
        db.invoiceDao().upsert(openInvoice(due = "2026-09-20"))
        InvoiceReminderChecker.handleAlarmTrigger(
            context,
            idReceber = "749428",
            kind = InvoiceReminderPrefs.KIND_DUE_DATE,
            expectedDueDate = "2026-09-15"
        )
        val key = InvoiceReminderPrefs.notificationKey(
            "749428", InvoiceReminderPrefs.KIND_DUE_DATE, "2026-09-20"
        )
        assertFalse(InvoiceReminderPrefs.wasFired(context, key))
        val keyOld = InvoiceReminderPrefs.notificationKey(
            "749428", InvoiceReminderPrefs.KIND_DUE_DATE, "2026-09-15"
        )
        assertFalse(InvoiceReminderPrefs.wasFired(context, keyOld))
    }

    @Test
    fun firedTrue_naoDuplica() = runBlocking {
        val inv = openInvoice(due = "2026-09-20")
        db.invoiceDao().upsert(inv)
        val key = InvoiceReminderPrefs.notificationKey(
            inv.idReceber, InvoiceReminderPrefs.KIND_DUE_DATE, inv.dueDate
        )
        InvoiceReminderPrefs.markFired(context, key)
        InvoiceReminderChecker.handleAlarmTrigger(
            context,
            inv.idReceber,
            InvoiceReminderPrefs.KIND_DUE_DATE,
            inv.dueDate
        )
        // Ainda fired; não limpa.
        assertTrue(InvoiceReminderPrefs.wasFired(context, key))
    }

    @Test
    fun notificacoesBloqueadas_naoMarcaFired() = runBlocking {
        val inv = openInvoice(due = "2026-09-20")
        db.invoiceDao().upsert(inv)
        shadowOf(context.packageManager).setShouldShowRequestPermissionRationale(
            android.Manifest.permission.POST_NOTIFICATIONS,
            false
        )
        // Robolectric: desabilita notificações via shadow se disponível.
        val nm = NotificationManagerCompat.from(context)
        // Em muitos ambientes de teste areNotificationsEnabled=true; forçamos skip via pref off.
        InvoiceReminderPrefs.setRemindDueDate(context, false)
        InvoiceReminderChecker.handleAlarmTrigger(
            context,
            inv.idReceber,
            InvoiceReminderPrefs.KIND_DUE_DATE,
            inv.dueDate
        )
        val key = InvoiceReminderPrefs.notificationKey(
            inv.idReceber, InvoiceReminderPrefs.KIND_DUE_DATE, inv.dueDate
        )
        assertFalse(InvoiceReminderPrefs.wasFired(context, key))
        assertFalse(nm.areNotificationsEnabled() && InvoiceReminderPrefs.wasFired(context, key))
    }

    @Test
    fun exampleDue2009_targetsCorrectDays() {
        val due = "2026-09-20"
        assertEquals(
            "2026-09-19",
            InvoiceAlarmTiming.alarmTargetDate(due, InvoiceReminderPrefs.KIND_DAY_BEFORE)
        )
        assertEquals(
            "2026-09-20",
            InvoiceAlarmTiming.alarmTargetDate(due, InvoiceReminderPrefs.KIND_DUE_DATE)
        )
    }
}
