package com.sa.healthtest.services.samsungHealth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.FitConnection
import com.samsung.android.sdk.healthdata.*

class SamsungHealthService : FitConnection, HealthDataStore.ConnectionListener {

    private var mReporter: StepCountReporter? = null
    private var mHealthDataStore: HealthDataStore? = null

    var serviceConnectionListener: ConnectCallback? = null

    var context: Context? = null
        set(value) {
            field = value
            initSdk(value)
        }

    private var mActivity = context as Activity?

    private fun initSdk(context: Context?) {
        try {
            HealthDataService().also { it.initialize(context) }
        } catch (e: Exception) {
            with("Error initializing HealthDataService") {
                Log.e(TAG, this, e)
                serviceConnectionListener?.error(this)
            }
        }
    }

    override fun connect() {
        mHealthDataStore = HealthDataStore(context, this).also { it.connectService() }
    }

    override fun disconnect() {
        mHealthDataStore?.disconnectService()
        serviceConnectionListener?.disconnected(this)
    }

    /**
     * Samsung heath connected successfully
     */
    override fun onConnected() {
        if (mHealthDataStore != null) {
            mReporter = StepCountReporter(mHealthDataStore!!)
            val isPermGranted = isPermissionAcquired()
            if (isPermGranted != null) {
                if (isPermGranted) {
                    serviceConnectionListener?.successConnected(this)
                    startReceivingData()
                } else {
                    requestPermission()
                }
            }
        }
    }

    /**
     * Samsung heath connection error
     */
    override fun onConnectionFailed(result: HealthConnectionErrorResult?) {
        serviceConnectionListener?.onPermissionDenied(this)
        serviceConnectionListener?.error(context?.getString(errorMessageByErrorCode(result?.errorCode)))
    }

    /**
     * Samsung heath disconnected. Is invoked only
     * if samsung health service crashes
     */
    override fun onDisconnected() {
        serviceConnectionListener?.disconnected(this)
    }

    private fun requestPermission() {
        val permKey = HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ)
        val pmsManager = HealthPermissionManager(mHealthDataStore)
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(setOf(permKey), mActivity)
                    .setResultListener { result ->
                        val resultMap = result.resultMap

                        if (resultMap.containsValue(java.lang.Boolean.FALSE)) {
                            serviceConnectionListener?.error(context?.getString(R.string.permission_acquired))
                            serviceConnectionListener?.onPermissionDenied(this)
                        } else {
                            // Get the current step count and display it
                            startReceivingData()
                            serviceConnectionListener?.successConnected(this)
                        }
                    }
        } catch (e: Exception) {
            with(context?.getString(R.string.error_perm_setting)) {
                Log.e(TAG, this, e)
                serviceConnectionListener?.error(this)
            }
        }
    }

    private fun startReceivingData() {

        mReporter?.start {
            serviceConnectionListener?.updateFitData(
                    FitResponse(this::class.java.simpleName,
                            stepCount = it,
                            icon = R.drawable.ic_samsung_fit,
                            isConnected = true))
        }
    }

    private fun isPermissionAcquired(): Boolean? {
        val permKey = HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ)
        val pmsManager = HealthPermissionManager(mHealthDataStore)
        try {
            // Check whether the permissions that this application needs are acquired
            val resultMap = pmsManager.isPermissionAcquired(setOf(permKey))
            val result = resultMap[permKey]
            return result ?: false
        } catch (e: Exception) {
            with("Permission request failed") {
                Log.e(TAG, this, e)
                serviceConnectionListener?.onPermissionDenied(this@SamsungHealthService)
                serviceConnectionListener?.error(this)
            }
        }
        return false
    }

    internal fun errorMessageByErrorCode(errorCode: Int?): Int {

        if (errorCode == null) return R.string.unknown_error

        return when (errorCode) {
            HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED -> R.string.req_install
            HealthConnectionErrorResult.OLD_VERSION_PLATFORM -> R.string.req_update
            HealthConnectionErrorResult.PLATFORM_DISABLED -> R.string.req_enable
            else -> R.string.req_availability
        }
    }

    companion object {
        val TAG = SamsungHealthService::class.java.simpleName
    }
}
