package com.example.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.webkit.CookieManager

/**
 * Decisões mínimas de startup offline — não altera cookies/sessão IXC.
 */
object OfflineStartup {
    private const val TAG = "OFFLINE_STARTUP"
    private const val PREFS = "alfatech_offline_access"
    private const val KEY_HAD_AUTH = "had_authenticated_session"

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        // Sem VALIDATED: no cold start a validação pode atrasar e forçar falso offline.
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Cookies persistidos do domínio da Central (não limpa nada). */
    fun hasCookieSession(centralBaseUrl: String): Boolean {
        return try {
            val cookie = CookieManager.getInstance().getCookie(centralBaseUrl)
            !cookie.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    fun markAuthenticatedSession(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAD_AUTH, true)
            .apply()
    }

    fun hadAuthenticatedSession(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAD_AUTH, false)
    }

    fun logDecision(
        networkAvailable: Boolean,
        localSessionAvailable: Boolean,
        localDataAvailable: Boolean,
        navigateHomeOffline: Boolean
    ) {
        Log.i(TAG, "networkAvailable=$networkAvailable")
        Log.i(TAG, "localSessionAvailable=$localSessionAvailable")
        Log.i(TAG, "localDataAvailable=$localDataAvailable")
        Log.i(TAG, "navigateHomeOffline=$navigateHomeOffline")
    }
}
