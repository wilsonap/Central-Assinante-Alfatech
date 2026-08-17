package com.example.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Runtime POST_NOTIFICATIONS + atalho oficial para configurações de notificação do app.
 * Não solicita repetidamente após a primeira negativa; não altera permissões OEM.
 */
object NotificationPermissionAssistant {

    private const val TAG = "NOTIFICATION_PERMISSION"
    private const val PREFS = "notification_permission_assistant"
    private const val KEY_RUNTIME_ASKED = "post_notifications_runtime_asked"
    private const val KEY_DENIED_GUIDANCE_DONE = "post_notifications_denied_guidance_done"

    fun hasPostNotificationsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Primeira solicitação runtime (Android 13+). */
    fun shouldRequestRuntime(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (hasPostNotificationsPermission(context)) return false
        return !prefs(context).getBoolean(KEY_RUNTIME_ASKED, false)
    }

    fun markRuntimeAsked(context: Context) {
        prefs(context).edit().putBoolean(KEY_RUNTIME_ASKED, true).apply()
        Log.i(TAG, "runtime_asked=true")
    }

    /**
     * Orientação discreta após negar (uma vez).
     * Só quando já pedimos e continua sem permissão.
     */
    fun shouldShowDeniedGuidance(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (hasPostNotificationsPermission(context)) return false
        if (!prefs(context).getBoolean(KEY_RUNTIME_ASKED, false)) return false
        return !prefs(context).getBoolean(KEY_DENIED_GUIDANCE_DONE, false)
    }

    fun markDeniedGuidanceDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_DENIED_GUIDANCE_DONE, true).apply()
        Log.i(TAG, "denied_guidance_done=true")
    }

    fun openAppNotificationSettings(context: Context) {
        val pkg = context.packageName
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
            putExtra("app_package", pkg)
            putExtra("app_uid", context.applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            Log.i(TAG, "settings_opened=APP_NOTIFICATION_SETTINGS")
        } catch (e: Exception) {
            Log.w(TAG, "settings_open_failed=${e.javaClass.simpleName}")
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$pkg")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
                Log.i(TAG, "settings_opened=APPLICATION_DETAILS_SETTINGS")
            } catch (e2: Exception) {
                Log.w(TAG, "fallback_failed=${e2.javaClass.simpleName}")
            }
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
