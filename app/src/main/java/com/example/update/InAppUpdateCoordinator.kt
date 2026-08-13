package com.example.update

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Coordenador isolado de In-App Updates (fase 1: somente app em foreground, FLEXIBLE).
 * Não bloqueia login, WebView, FCM nem faturas.
 */
class InAppUpdateCoordinator(
    private val activity: ComponentActivity
) {
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)

    private val checkInFlight = AtomicBoolean(false)
    private val flexibleFlowStartedThisSession = AtomicBoolean(false)
    private val readyUiShown = AtomicBoolean(false)

    private val updateLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "flow_resultCode=${result.resultCode}")
        }
    }

    private val installListener = InstallStateUpdatedListener { state ->
        val status = state.installStatus()
        Log.i(TAG, "installStatus=${installStatusName(status)}")
        if (status == InstallStatus.DOWNLOADED) {
            showReadyToInstallUi()
        }
    }

    fun onCreate() {
        appUpdateManager.registerListener(installListener)
    }

    fun onDestroy() {
        try {
            appUpdateManager.unregisterListener(installListener)
        } catch (_: Exception) {
        }
    }

    /**
     * Chamado quando a Activity entra em foreground.
     * - Retoma DOWNLOADED se necessário.
     * - Inicia fluxo FLEXIBLE no máximo uma vez por sessão de processo.
     */
    fun checkOnForeground() {
        if (!checkInFlight.compareAndSet(false, true)) {
            Log.i(TAG, "check_skipped=in_flight")
            return
        }
        Log.i(TAG, "check_started=true")
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                checkInFlight.set(false)
                try {
                    handleAppUpdateInfo(info)
                } catch (e: Exception) {
                    Log.i(TAG, "check_failed=true")
                    Log.i(TAG, "fail=${e.javaClass.simpleName}")
                }
            }
            .addOnFailureListener { e ->
                checkInFlight.set(false)
                Log.i(TAG, "check_failed=true")
                Log.i(TAG, "fail=${e.javaClass.simpleName}")
            }
    }

    private fun handleAppUpdateInfo(info: AppUpdateInfo) {
        val availability = availabilityName(info.updateAvailability())
        Log.i(TAG, "availability=$availability")
        Log.i(TAG, "availableVersionCode=${info.availableVersionCode()}")
        Log.i(TAG, "installStatus=${installStatusName(info.installStatus())}")

        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            showReadyToInstallUi()
            return
        }

        // Já baixando: só observar; não reabrir diálogo.
        if (info.installStatus() == InstallStatus.DOWNLOADING ||
            info.installStatus() == InstallStatus.PENDING
        ) {
            Log.i(TAG, "flow_skipped=already_in_progress")
            return
        }

        val flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        Log.i(TAG, "flexibleAllowed=$flexibleAllowed")

        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            return
        }
        if (!flexibleAllowed) {
            return
        }
        if (!flexibleFlowStartedThisSession.compareAndSet(false, true)) {
            Log.i(TAG, "flow_skipped=already_started_this_session")
            return
        }

        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                options
            )
            Log.i(TAG, "flow_started=true")
        } catch (e: Exception) {
            flexibleFlowStartedThisSession.set(false)
            Log.i(TAG, "check_failed=true")
            Log.i(TAG, "fail=${e.javaClass.simpleName}")
        }
    }

    private fun showReadyToInstallUi() {
        if (activity.isFinishing) return
        if (!readyUiShown.compareAndSet(false, true)) {
            return
        }
        Log.i(TAG, "installStatus=DOWNLOADED")
        try {
            AlertDialog.Builder(activity)
                .setMessage("Atualização pronta para instalar")
                .setPositiveButton("Atualizar agora") { _, _ ->
                    Log.i(TAG, "completeUpdate=true")
                    try {
                        appUpdateManager.completeUpdate()
                    } catch (e: Exception) {
                        Log.i(TAG, "check_failed=true")
                        Log.i(TAG, "fail=${e.javaClass.simpleName}")
                        readyUiShown.set(false)
                    }
                }
                .setNegativeButton("Depois") { _, _ ->
                    readyUiShown.set(false)
                }
                .setOnCancelListener {
                    readyUiShown.set(false)
                }
                .show()
        } catch (e: Exception) {
            readyUiShown.set(false)
            Log.i(TAG, "check_failed=true")
            Log.i(TAG, "fail=${e.javaClass.simpleName}")
        }
    }

    private fun availabilityName(value: Int): String = when (value) {
        UpdateAvailability.UPDATE_AVAILABLE -> "UPDATE_AVAILABLE"
        UpdateAvailability.UPDATE_NOT_AVAILABLE -> "UPDATE_NOT_AVAILABLE"
        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
            "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS"
        UpdateAvailability.UNKNOWN -> "UNKNOWN"
        else -> "OTHER_$value"
    }

    private fun installStatusName(value: Int): String = when (value) {
        InstallStatus.UNKNOWN -> "UNKNOWN"
        InstallStatus.PENDING -> "PENDING"
        InstallStatus.DOWNLOADING -> "DOWNLOADING"
        InstallStatus.DOWNLOADED -> "DOWNLOADED"
        InstallStatus.INSTALLING -> "INSTALLING"
        InstallStatus.INSTALLED -> "INSTALLED"
        InstallStatus.FAILED -> "FAILED"
        InstallStatus.CANCELED -> "CANCELED"
        else -> "OTHER_$value"
    }

    companion object {
        private const val TAG = "APP_UPDATE"
    }
}
