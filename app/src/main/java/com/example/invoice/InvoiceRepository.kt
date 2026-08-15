package com.example.invoice

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InvoiceRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = AppDatabase.getDatabase(appContext).invoiceDao()

    fun observeAll(): Flow<List<InvoiceEntity>> = dao.observeAll()

    suspend fun latestSyncAt(): Long? = withContext(Dispatchers.IO) { dao.latestSyncAt() }

    suspend fun syncFromGetFaturasJson(rawJson: String): Int = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val parsed = InvoiceParser.parseGetFaturasJson(rawJson, now)
        if (parsed.invoices.isEmpty()) {
            Log.i("INVOICE_SYNC", "upsert invoiceCount=0 syncSuccess=${parsed.skipped == 0}")
            return@withContext 0
        }
        parsed.invoices.forEach { entity ->
            val previous = dao.findByIdReceber(entity.idReceber)
            dao.upsert(entity)
            val current = dao.findByIdReceber(entity.idReceber) ?: entity
            applyAlarmAfterUpsert(previous, current)
            Log.i(
                "INVOICE_SYNC",
                "upsert idReceber=${InvoiceParser.maskId(entity.idReceber)} " +
                    "status=${entity.status} billingType=${entity.billingType} " +
                    "group=${entity.sourceGroup}"
            )
        }
        Log.i(
            "INVOICE_SYNC",
            "upsert done invoiceCount=${parsed.invoices.size} skipped=${parsed.skipped} syncSuccess=true"
        )
        parsed.invoices.size
    }

    /**
     * Alarmes por fatura após sync. Só cancela com status terminal explícito do IXC.
     */
    private fun applyAlarmAfterUpsert(previous: InvoiceEntity?, current: InvoiceEntity) {
        if (InvoiceAlarmTiming.isTerminalInvoice(current)) {
            previous?.let { InvoiceAlarmScheduler.cancelInvoice(appContext, it) }
            InvoiceAlarmScheduler.cancelInvoice(appContext, current)
            return
        }
        if (previous != null && previous.dueDate != current.dueDate) {
            InvoiceAlarmScheduler.rescheduleInvoice(appContext, previous, current)
            return
        }
        InvoiceAlarmScheduler.scheduleInvoice(appContext, current)
    }
}
