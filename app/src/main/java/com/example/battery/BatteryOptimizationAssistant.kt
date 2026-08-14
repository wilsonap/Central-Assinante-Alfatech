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
 * Assistente de otimização de bateria.
 * Não altera configurações automaticamente — apenas detecta e abre a tela do sistema/OEM.
 */
object BatteryOptimizationAssistant {

    private const val TAG = "BATTERY_OPTIMIZATION"
    private const val PREFS = "battery_optimization_assistant"
    private const val KEY_PROMPT_DONE = "prompt_acknowledged"

    /**
     * true = o app ainda está sob otimização de bateria (pode restringir background).
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
    }

    fun hasAcknowledgedPrompt(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PROMPT_DONE, false)
    }

    fun markPromptAcknowledged(context: Context) {
        prefs(context).edit().putBoolean(KEY_PROMPT_DONE, true).apply()
    }

    fun shouldShowPrompt(context: Context): Boolean {
        if (hasAcknowledgedPrompt(context)) return false
        return isBatteryOptimizationEnabled(context)
    }

    /**
     * Abre a tela de configurações mais adequada ao fabricante.
     * Não solicita isenção automaticamente.
     */
    fun openBackgroundSettings(context: Context) {
        val intents = buildManufacturerIntents(context)
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
        // Fallback final: detalhes do app
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

    private fun buildManufacturerIntents(context: Context): List<Intent> {
        val pkg = context.packageName
        val manufacturer = Build.MANUFACTURER.orEmpty().lowercase()
        val list = mutableListOf<Intent>()

        fun activity(pkgName: String, cls: String): Intent =
            Intent().setComponent(ComponentName(pkgName, cls))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ||
                manufacturer.contains("poco") -> {
                list += activity(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                list += activity(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                list += activity(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
                list += activity(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                list += activity(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
                list += activity(
                    "com.oplus.battery",
                    "com.oplus.battery.ui.BatteryActivity"
                )
            }
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                list += activity(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
                list += activity(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            }
            manufacturer.contains("samsung") -> {
                list += Intent().setComponent(
                    ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.battery.ui.BatteryActivity"
                    )
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                list += Intent().setComponent(
                    ComponentName(
                        "com.samsung.android.sm",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            manufacturer.contains("oneplus") -> {
                list += activity(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
            }
        }

        // Telas padrão Android (não solicitam isenção automática).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            list += Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        list += Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return list
    }
}
