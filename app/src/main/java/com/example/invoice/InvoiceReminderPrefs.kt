package com.example.invoice

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferências de lembrete de faturas (padrão: ambos ligados) + dedupe fired.
 */
object InvoiceReminderPrefs {
    private const val PREFS = "invoice_reminder_prefs"
    private const val KEY_DAY_BEFORE = "remind_day_before"
    private const val KEY_DUE_DATE = "remind_due_date"
    private const val KEY_FIRED_PREFIX = "fired_"

    const val KIND_DAY_BEFORE = "day_before"
    const val KIND_DUE_DATE = "due_date"

    private val _remindDayBefore = MutableStateFlow(true)
    val remindDayBefore: StateFlow<Boolean> = _remindDayBefore.asStateFlow()

    private val _remindDueDate = MutableStateFlow(true)
    val remindDueDate: StateFlow<Boolean> = _remindDueDate.asStateFlow()

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun hydrate(context: Context) {
        val p = prefs(context)
        _remindDayBefore.value = p.getBoolean(KEY_DAY_BEFORE, true)
        _remindDueDate.value = p.getBoolean(KEY_DUE_DATE, true)
    }

    fun isRemindDayBeforeEnabled(context: Context): Boolean {
        val v = prefs(context).getBoolean(KEY_DAY_BEFORE, true)
        _remindDayBefore.value = v
        return v
    }

    fun isRemindDueDateEnabled(context: Context): Boolean {
        val v = prefs(context).getBoolean(KEY_DUE_DATE, true)
        _remindDueDate.value = v
        return v
    }

    fun setRemindDayBefore(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DAY_BEFORE, enabled).apply()
        _remindDayBefore.value = enabled
    }

    fun setRemindDueDate(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DUE_DATE, enabled).apply()
        _remindDueDate.value = enabled
    }

    /** Identificador estável: invoice_[idReceber]_day_before / due_date + dueDate. */
    fun notificationKey(idReceber: String, kind: String, dueDate: String): String =
        "invoice_${idReceber}_${kind}_$dueDate"

    fun wasFired(context: Context, key: String): Boolean =
        prefs(context).getBoolean(KEY_FIRED_PREFIX + key, false)

    fun markFired(context: Context, key: String) {
        prefs(context).edit().putBoolean(KEY_FIRED_PREFIX + key, true).apply()
    }
}
