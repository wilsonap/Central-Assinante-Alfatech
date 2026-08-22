package com.example.battery

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Orientação opcional somente para Xiaomi / Redmi / POCO (MIUI).
 * Não altera configurações automaticamente — abre telas do sistema/OEM.
 * Demais fabricantes não recebem dialog (AlarmManager principal já validado).
 */
object BatteryOptimizationAssistant {

    private const val TAG = "BATTERY_OPTIMIZATION"
    private const val PREFS = "battery_optimization_assistant"
    private const val KEY_PROMPT_DONE = "prompt_acknowledged"

    /**
     * true = o app ainda está sob otimização de bateria (AOSP).
     * false = já está isento / sem restrição detectável.
     */
    fun isBatteryOptimizationEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val pm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return false
        val ignoring = pm.isIgnoringBatteryOptimizations(context.packageName)
        return !ignoring
    }

    fun logStatus(context: Context) {
        val enabled = isBatteryOptimizationEnabled(context)
        Log.i(TAG, "enabled=$enabled")
        Log.i(TAG, "xiaomiFamily=${isXiaomiFamily()}")
    }

    fun hasAcknowledgedPrompt(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PROMPT_DONE, false)
    }

    fun markPromptAcknowledged(context: Context) {
        prefs(context).edit().putBoolean(KEY_PROMPT_DONE, true).apply()
    }

    /**
     * Dialog só em Xiaomi/Redmi/POCO, uma vez.
     * Outros fabricantes: nunca.
     */
    fun shouldShowPrompt(context: Context): Boolean {
        if (!isXiaomiFamily()) return false
        if (hasAcknowledgedPrompt(context)) return false
        return true
    }

    /** Xiaomi / Redmi / POCO via MANUFACTURER ou BRAND. */
    fun isXiaomiFamily(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty().lowercase()
        val brand = Build.BRAND.orEmpty().lowercase()
        fun match(value: String): Boolean =
            value.contains("xiaomi") || value.contains("redmi") || value.contains("poco")
        return match(manufacturer) || match(brand)
    }

    /**
     * Abre Autostart / powerkeeper / detalhes do app (MIUI).
     * Não solicita isenção automaticamente.
     */
    fun openBackgroundSettings(context: Context) {
        val intents = buildXiaomiIntents(context)
        for (intent in intents) {
            try {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    Log.i(TAG, "settings_opened=${intent.component ?: intent.action}")
                    return
                }
            } catch (_: Exception) {
                // tenta próximo
            }
        }
        try {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            Log.i(TAG, "settings_opened=APPLICATION_DETAILS_SETTINGS")
        } catch (e: Exception) {
            Log.w(TAG, "settings_open_failed=${e.javaClass.simpleName}")
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun buildXiaomiIntents(context: Context): List<Intent> {
        val pkg = context.packageName
        val list = mutableListOf<Intent>()

        fun activity(pkgName: String, cls: String): Intent =
            Intent().setComponent(ComponentName(pkgName, cls))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Autostart → apps ocultos / sem restrições → lista AOSP → detalhes
        list += activity(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        list += activity(
            "com.miui.powerkeeper",
            "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            list += Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        list += Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
            putExtra("app_package", pkg)
            putExtra("app_uid", context.applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        list += Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return list
    }
}
