package com.sa.healthtest.services.samsungHealth

import android.app.Activity
import android.util.Log
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.FitConnection
import com.samsung.android.sdk.healthdata.*

class SamsungHealthService(private val mActivity: Activity): FitConnection, HealthDataStore.ConnectionListener {

    private lateinit var mReporter: StepCountReporter
    private lateinit var mHealthDataStore: HealthDataStore
    private val mCallBack = (mActivity as ConnectCallback)

    init {
        initSdk()
    }

    private fun initSdk() {
        try {
            HealthDataService().also { it.initialize(mActivity) }
        } catch (e: Exception) {
            with("Error initializing HealthDataService") {
                Log.e(TAG, this, e)
                mCallBack.error(this)
            }
        }
    }

    override fun connect() {
        mHealthDataStore = HealthDataStore(mActivity, this)
                .also { it.connectService() }
    }

    override fun disconnect() {
        mHealthDataStore.disconnectService()
        mCallBack.disconnected(this)
    }

    /**
     * Samsung heath connected successfully
     */
    override fun onConnected() {
        mReporter = StepCountReporter(mHealthDataStore)
        val isPermGranted = isPermissionAcquired()
        if (isPermGranted != null) {
            if (isPermGranted) {
                mCallBack.successConnected(this)
                startReceivingData()
            } else {
                requestPermission()
            }
        }
    }

    /**
     * Samsung heath connection error
     */
    override fun onConnectionFailed(result: HealthConnectionErrorResult?) {
        mCallBack.onPermissionDenied(this)
        mCallBack.error(when (result?.errorCode) {
            HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED -> mActivity.getString(R.string.req_install)
            HealthConnectionErrorResult.OLD_VERSION_PLATFORM -> mActivity.getString(R.string.req_update)
            HealthConnectionErrorResult.PLATFORM_DISABLED -> mActivity.getString(R.string.req_enable)
            else -> mActivity.getString(R.string.req_availability)
        })
    }

    /**
     * Samsung heath disconnected. Is invoked only
     * if samsung health service crashes
     */
    override fun onDisconnected() {
        mCallBack.disconnected(this)
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
                            mCallBack.error(mActivity.getString(R.string.permission_acquired))
                            mCallBack.onPermissionDenied(this)
                        } else {
                            // Get the current step count and display it
                            startReceivingData()
                            mCallBack.successConnected(this)
                        }
                    }
        } catch (e: Exception) {
            with(mActivity.getString(R.string.error_perm_setting)) {
                Log.e(TAG, this, e)
                mCallBack.error(this)
            }
        }
    }

    private fun startReceivingData() {
        mReporter.start {
            mCallBack.updateFitData(
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
                mCallBack.onPermissionDenied(this@SamsungHealthService)
                mCallBack.error(this)
            }
        }
        return false
    }

    companion object {
        val TAG = SamsungHealthService::class.java.simpleName
    }
}
