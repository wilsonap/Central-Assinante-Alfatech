package com.example.invoice

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import com.example.notifications.NotificationChannels
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Fonte única da lógica de lembretes (MainActivity + Worker).
 * Serializa wasFired → validação → notify → markFired.
 */
object InvoiceReminderChecker {

    private const val TAG = "INVOICE_REMINDER"
    private val mutex = Mutex()

    /** true se o canal existe mas está silenciado (IMPORTANCE_NONE) — UI pode orientar. */
    @Volatile
    var channelNeedsUserAttention: Boolean = false
        private set

    /**
     * @param trigger app_start | invoice_sync | worker | worker_fallback
     * app_start / invoice_sync: NÃO notificam (AlarmManager é principal).
     * worker: só fallback após 13:00 se ainda wasFired=false.
     */
    suspend fun run(context: Context, trigger: String) = mutex.withLock {
        val appContext = context.applicationContext
        Log.i(TAG, "trigger=$trigger")
        if (trigger == "worker" || trigger == "worker_fallback") {
            Log.i(TAG, "workerStarted=true")
            InvoiceReminderDiagnostics.markWorkerRun(appContext)
        }
        InvoiceReminderDiagnostics.markCheck(appContext)
        NotificationChannels.ensureCreated(appContext)

        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val nowCal = Calendar.getInstance()
        val today = iso.format(nowCal.time)
        val hour = nowCal.get(Calendar.HOUR_OF_DAY)
        val tomorrowCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrow = iso.format(tomorrowCal.time)
        Log.i(TAG, "today=$today")

        val dao = AppDatabase.getDatabase(appContext).invoiceDao()
        val remindBefore = InvoiceReminderPrefs.isRemindDayBeforeEnabled(appContext)
        val remindToday = InvoiceReminderPrefs.isRemindDueDateEnabled(appContext)

        val candidates = mutableListOf<Pair<InvoiceEntity, String>>()
        if (remindBefore) {
            dao.findOpenByDueDate(tomorrow).forEach { inv ->
                candidates += inv to InvoiceReminderPrefs.KIND_DAY_BEFORE
            }
        }
        if (remindToday) {
            dao.findOpenByDueDate(today).forEach { inv ->
                candidates += inv to InvoiceReminderPrefs.KIND_DUE_DATE
            }
        }

        val allForLog = LinkedHashSet<InvoiceEntity>()
        allForLog.addAll(dao.findOpenByDueDate(today))
        allForLog.addAll(dao.findOpenByDueDate(tomorrow))
        Log.i(TAG, "invoicesChecked=${allForLog.size}")

        allForLog.forEach { inv ->
            val kind = when (inv.dueDate) {
                today -> InvoiceReminderPrefs.KIND_DUE_DATE
                tomorrow -> InvoiceReminderPrefs.KIND_DAY_BEFORE
                else -> "other"
            }
            val prefOn = when (kind) {
                InvoiceReminderPrefs.KIND_DUE_DATE -> remindToday
                InvoiceReminderPrefs.KIND_DAY_BEFORE -> remindBefore
                else -> false
            }
            val eligible = prefOn && !InvoiceAlarmTiming.isTerminalInvoice(inv)
            val key = InvoiceReminderPrefs.notificationKey(inv.idReceber, kind, inv.dueDate)
            val already = InvoiceReminderPrefs.wasFired(appContext, key)
            Log.i(TAG, "dedupeKey=$key")
            Log.i(TAG, "wasFired=$already")
            logCandidate(inv, kind, eligible, already)
        }

        when (trigger) {
            "app_start" -> {
                candidates.forEach { (inv, kind) ->
                    Log.i(TAG, "trigger=app_start")
                    Log.i(TAG, "action=no_notify_alarm_managed")
                    Log.i(
                        TAG,
                        "idReceber=${InvoiceParser.maskId(inv.idReceber)} kind=$kind"
                    )
                }
            }
            "invoice_sync" -> {
                Log.i(TAG, "trigger=invoice_sync")
                Log.i(TAG, "action=schedule_only")
            }
            "worker", "worker_fallback" -> {
                if (hour < InvoiceAlarmTiming.CUTOFF_HOUR) {
                    Log.i(TAG, "trigger=worker_fallback")
                    Log.i(TAG, "skipReason=waiting_for_alarm")
                } else {
                    candidates.forEach { (inv, kind) ->
                        maybeNotify(
                            context = appContext,
                            invoice = inv,
                            kind = kind,
                            logTag = TAG,
                            trigger = "worker_fallback",
                            markFiredCaller = "InvoiceReminderWorker",
                            isFallback = true
                        )
                    }
                }
            }
            else -> {
                Log.w(TAG, "unknown trigger=$trigger — no notify")
            }
        }

        if (trigger == "worker" || trigger == "worker_fallback") {
            Log.i(TAG, "workerFinished=true")
        }
    }

    /**
     * Disparo AlarmManager: Room é a fonte final (não confiar só nos extras).
     */
    suspend fun handleAlarmTrigger(
        context: Context,
        idReceber: String,
        kind: String,
        expectedDueDate: String
    ) = mutex.withLock {
        val appContext = context.applicationContext
        val alarmTag = "INVOICE_ALARM"
        Log.i(TAG, "trigger=alarm")
        NotificationChannels.ensureCreated(appContext)

        val dao = AppDatabase.getDatabase(appContext).invoiceDao()
        val invoice = dao.findByIdReceber(idReceber)
        val roomFound = invoice != null
        Log.i(alarmTag, "roomFound=$roomFound")
        if (invoice == null) {
            Log.i(alarmTag, "notificationPosted=false")
            Log.i(alarmTag, "skipReason=room_not_found")
            InvoiceReminderDiagnostics.markSkip(appContext, "room_not_found")
            return@withLock
        }

        Log.i(alarmTag, "status=${invoice.status}")
        val dueDateMatches = invoice.dueDate == expectedDueDate
        Log.i(alarmTag, "dueDateMatches=$dueDateMatches")
        if (!dueDateMatches) {
            Log.i(alarmTag, "notificationPosted=false")
            Log.i(alarmTag, "skipReason=due_date_mismatch")
            InvoiceReminderDiagnostics.markSkip(appContext, "due_date_mismatch")
            return@withLock
        }

        if (InvoiceAlarmTiming.isTerminalInvoice(invoice)) {
            Log.i(alarmTag, "notificationPosted=false")
            Log.i(alarmTag, "skipReason=terminal_status")
            InvoiceReminderDiagnostics.markSkip(appContext, "terminal_status")
            return@withLock
        }

        val prefOn = when (kind) {
            InvoiceReminderPrefs.KIND_DAY_BEFORE ->
                InvoiceReminderPrefs.isRemindDayBeforeEnabled(appContext)
            InvoiceReminderPrefs.KIND_DUE_DATE ->
                InvoiceReminderPrefs.isRemindDueDateEnabled(appContext)
            else -> false
        }
        if (!prefOn) {
            Log.i(alarmTag, "notificationPosted=false")
            Log.i(alarmTag, "skipReason=pref_disabled")
            InvoiceReminderDiagnostics.markSkip(appContext, "pref_disabled")
            return@withLock
        }

        maybeNotify(
            context = appContext,
            invoice = invoice,
            kind = kind,
            logTag = alarmTag,
            trigger = "alarm",
            markFiredCaller = "InvoiceReminderReceiver",
            isFallback = false
        )
    }

    fun openInvoiceChannelSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.CHANNEL_INVOICES)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "open_channel_settings_fail=${e.javaClass.simpleName}")
        }
    }

    private fun logCandidate(
        invoice: InvoiceEntity,
        kind: String,
        eligible: Boolean,
        alreadyNotified: Boolean
    ) {
        val kindLog = when (kind) {
            InvoiceReminderPrefs.KIND_DUE_DATE -> "due_today"
            InvoiceReminderPrefs.KIND_DAY_BEFORE -> "day_before"
            else -> kind
        }
        Log.i(TAG, "idReceber=${InvoiceParser.maskId(invoice.idReceber)}")
        Log.i(TAG, "dueDate=${invoice.dueDate}")
        Log.i(TAG, "status=${invoice.status}")
        Log.i(TAG, "eligible=$eligible")
        Log.i(TAG, "kind=$kindLog")
        Log.i(TAG, "alreadyNotified=$alreadyNotified")
    }

    private suspend fun maybeNotify(
        context: Context,
        invoice: InvoiceEntity,
        kind: String,
        logTag: String,
        trigger: String,
        markFiredCaller: String,
        isFallback: Boolean
    ) {
        val todayIsDue = kind == InvoiceReminderPrefs.KIND_DUE_DATE
        val kindLog = if (todayIsDue) "due_today" else "day_before"

        Log.i(TAG, "trigger=$trigger")
        if (isFallback) {
            Log.i(TAG, "fallback=true")
            Log.i(TAG, "fallbackReason=alarm_not_fired")
        }

        if (InvoiceAlarmTiming.isTerminalInvoice(invoice)) {
            skip(context, "closed", notifyAttempt = false, logTag = logTag)
            return
        }

        val key = InvoiceReminderPrefs.notificationKey(invoice.idReceber, kind, invoice.dueDate)
        Log.i(logTag, "dedupeKey=$key")
        val already = InvoiceReminderPrefs.wasFired(context, key)
        Log.i(logTag, "wasFired=$already")
        if (already) {
            // Compat: lembretes antigos sem histórico — backfill sem reenviar.
            ensureInvoiceHistoryEntry(
                context = context,
                dedupeKey = key,
                kind = kind,
                backfill = true
            )
            skip(context, "already_fired", notifyAttempt = false, logTag = logTag)
            return
        }

        val gate = validateNotificationGate(context)
        Log.i(logTag, "notificationsEnabled=${gate.notificationsEnabled}")
        Log.i(logTag, "channelImportance=${gate.channelImportance}")
        if (com.example.BuildConfig.DEBUG) {
            Log.i(logTag, "channelVibrationEnabled=${gate.channelVibrationEnabled}")
        }
        if (!gate.ok) {
            channelNeedsUserAttention = gate.skipReason == "channel_importance_none" ||
                gate.skipReason == "channel_missing"
            skip(context, gate.skipReason, notifyAttempt = false, logTag = logTag)
            return
        }
        channelNeedsUserAttention = false

        val title = if (todayIsDue) {
            "Sua fatura Alfatech vence hoje"
        } else {
            "Sua fatura Alfatech vence amanhã"
        }
        val valueLine = "Valor: ${formatBrl(invoice.amountCents)}"
        val tip = when (invoice.billingType) {
            InvoiceEntity.BILLING_BANK ->
                "Consulte o boleto na Central do Assinante."
            InvoiceEntity.BILLING_STORE ->
                "Procure um dos pontos de atendimento da Alfatech para realizar o pagamento."
            else ->
                "Acesse a Central do Assinante para consultar os detalhes."
        }
        val body = "$valueLine\n\n$tip"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_invoices", true)
        }
        val pending = PendingIntent.getActivity(
            context,
            key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.CHANNEL_INVOICES
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(valueLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(NotificationChannels.INVOICE_VIBRATION_PATTERN)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        Log.i(logTag, "notifyAttempt=true")
        InvoiceReminderDiagnostics.markAttempt(context)
        val nm = NotificationManagerCompat.from(context)
        try {
            nm.notify(key.hashCode(), notification)
            if (!nm.areNotificationsEnabled()) {
                skip(context, "notifications_disabled_after_notify", notifyAttempt = true, logTag = logTag)
                return
            }
            Log.i(logTag, "notificationPosted=true")
            InvoiceReminderDiagnostics.markPosted(context)
            ensureInvoiceHistoryEntry(
                context = context,
                dedupeKey = key,
                kind = kind,
                backfill = false
            )
            InvoiceReminderPrefs.markFired(context, key)
            Log.i(TAG, "markFiredCaller=$markFiredCaller")
            Log.i(TAG, "markFiredKey=$key")
            Log.i(logTag, "markFired=true")
            Log.i(
                logTag,
                "fired kind=$kindLog idReceber=${InvoiceParser.maskId(invoice.idReceber)} " +
                    "billingType=${invoice.billingType}"
            )
        } catch (e: SecurityException) {
            skip(context, e.javaClass.simpleName, notifyAttempt = true, logTag = logTag)
        } catch (e: Exception) {
            skip(context, e.javaClass.simpleName, notifyAttempt = true, logTag = logTag)
        }
    }

    /**
     * Histórico do sino independente do fired Android.
     * [backfill]=true quando already fired e só falta a entrada no Room.
     */
    private suspend fun ensureInvoiceHistoryEntry(
        context: Context,
        dedupeKey: String,
        kind: String,
        backfill: Boolean
    ) {
        val histTag = "NOTIFICATION_HISTORY"
        Log.i(histTag, "key=$dedupeKey")
        Log.i(histTag, "insertCaller=InvoiceReminderChecker.ensureInvoiceHistoryEntry")
        try {
            val dao = AppDatabase.getDatabase(context).notificationDao()
            val exists = dao.findByMessageId(dedupeKey) != null
            Log.i(histTag, "exists=$exists")
            if (exists) {
                Log.i(histTag, "backfill=false")
                Log.i(histTag, "inserted=false")
                return
            }
            Log.i(histTag, "backfill=$backfill")
            val inserted = com.example.data.PushNotificationRepository.persistInvoiceReminder(
                context = context,
                dedupeKey = dedupeKey,
                kind = kind
            )
            Log.i(histTag, "inserted=$inserted")
        } catch (e: Exception) {
            Log.i(histTag, "backfill=$backfill")
            Log.i(histTag, "inserted=false")
            Log.w(histTag, "persistFail=${e.javaClass.simpleName}")
        }
    }

    private data class GateResult(
        val ok: Boolean,
        val notificationsEnabled: Boolean,
        val channelImportance: Int,
        val channelVibrationEnabled: Boolean,
        val skipReason: String
    )

    private fun validateNotificationGate(context: Context): GateResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return GateResult(
                    ok = false,
                    notificationsEnabled = false,
                    channelImportance = -1,
                    channelVibrationEnabled = false,
                    skipReason = "post_notifications_denied"
                )
            }
        }

        val nmCompat = NotificationManagerCompat.from(context)
        val enabled = nmCompat.areNotificationsEnabled()
        if (!enabled) {
            return GateResult(
                ok = false,
                notificationsEnabled = false,
                channelImportance = -1,
                channelVibrationEnabled = false,
                skipReason = "notifications_disabled"
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = manager?.getNotificationChannel(NotificationChannels.CHANNEL_INVOICES)
            if (channel == null) {
                return GateResult(
                    ok = false,
                    notificationsEnabled = true,
                    channelImportance = -1,
                    channelVibrationEnabled = false,
                    skipReason = "channel_missing"
                )
            }
            val importance = channel.importance
            val vibrates = channel.shouldVibrate()
            if (importance == NotificationManager.IMPORTANCE_NONE) {
                return GateResult(
                    ok = false,
                    notificationsEnabled = true,
                    channelImportance = importance,
                    channelVibrationEnabled = vibrates,
                    skipReason = "channel_importance_none"
                )
            }
            return GateResult(
                ok = true,
                notificationsEnabled = true,
                channelImportance = importance,
                channelVibrationEnabled = vibrates,
                skipReason = ""
            )
        }

        return GateResult(
            ok = true,
            notificationsEnabled = true,
            channelImportance = NotificationManager.IMPORTANCE_HIGH,
            channelVibrationEnabled = true,
            skipReason = ""
        )
    }

    private fun skip(
        context: Context,
        reason: String,
        notifyAttempt: Boolean,
        logTag: String = TAG
    ) {
        if (!notifyAttempt) {
            Log.i(logTag, "notifyAttempt=false")
        }
        Log.i(logTag, "notificationPosted=false")
        Log.i(logTag, "skipReason=$reason")
        Log.i(logTag, "markFired=false")
        InvoiceReminderDiagnostics.markSkip(context, reason)
    }

    private fun formatBrl(cents: Long): String {
        val reais = cents / 100
        val frac = (cents % 100).toInt().toString().padStart(2, '0')
        return "R$ $reais,$frac"
    }
}
