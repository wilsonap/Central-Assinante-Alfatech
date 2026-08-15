package com.example.invoice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Disparo de alarme individual — valida Room antes de notificar.
 */
class InvoiceReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        if (intent.action != InvoiceAlarmScheduler.ACTION_REMINDER &&
            intent.data?.scheme != "alfatech"
        ) {
            // Aceita também se action vier nula mas data URI bate (alguns OEMs).
            if (intent.data?.scheme != "alfatech") return
        }

        val idReceber = intent.getStringExtra(InvoiceAlarmScheduler.EXTRA_ID_RECEBER)
            ?: intent.data?.pathSegments?.getOrNull(0)
        val kind = intent.getStringExtra(InvoiceAlarmScheduler.EXTRA_KIND)
            ?: intent.data?.pathSegments?.getOrNull(1)
        val dueDate = intent.getStringExtra(InvoiceAlarmScheduler.EXTRA_DUE_DATE)
            ?: intent.data?.pathSegments?.getOrNull(2)

        Log.i(TAG, "receiverStarted=true")
        Log.i(TAG, "idReceber=${InvoiceParser.maskId(idReceber.orEmpty())}")
        Log.i(TAG, "kind=$kind")

        if (idReceber.isNullOrBlank() || kind.isNullOrBlank() || dueDate.isNullOrBlank()) {
            Log.i(TAG, "skipReason=missing_extras")
            return
        }

        val pending = goAsync()
        scope.launch {
            try {
                InvoiceReminderChecker.handleAlarmTrigger(
                    context = context.applicationContext,
                    idReceber = idReceber,
                    kind = kind,
                    expectedDueDate = dueDate
                )
            } catch (e: Exception) {
                Log.w(TAG, "receiver fail=${e.javaClass.simpleName}")
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "INVOICE_ALARM"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
