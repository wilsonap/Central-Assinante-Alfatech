package com.example.speedtest

/**
 * Hosts permitidos na WebView do Speedtest Custom (Ookla).
 * Navegação principal: alfatechtelecom.speedtestcustom.com
 * Assets/API Ookla no mesmo domínio *.speedtestcustom.com.
 */
object SpeedTestHosts {
    const val START_URL = "https://alfatechtelecom.speedtestcustom.com/"
    const val PRIMARY_HOST = "alfatechtelecom.speedtestcustom.com"

    fun isAllowedHost(host: String?): Boolean {
        val h = host?.lowercase()?.trim().orEmpty()
        if (h.isEmpty()) return false
        return h == PRIMARY_HOST ||
            h == "speedtestcustom.com" ||
            h.endsWith(".speedtestcustom.com")
    }
}
