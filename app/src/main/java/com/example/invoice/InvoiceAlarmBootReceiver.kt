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
 * Reconstrói alarmes após BOOT_COMPLETED ou MY_PACKAGE_REPLACED.
 */
class InvoiceAlarmBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        Log.i(TAG, "bootOrReplace action=$action")
        val pending = goAsync()
        scope.launch {
            try {
                InvoiceAlarmScheduler.scheduleAllOpenInvoices(context.applicationContext)
            } catch (e: Exception) {
                Log.w(TAG, "bootReschedule fail=${e.javaClass.simpleName}")
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
