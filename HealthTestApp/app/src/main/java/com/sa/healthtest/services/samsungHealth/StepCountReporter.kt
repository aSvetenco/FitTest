package com.sa.healthtest.services.samsungHealth

import android.util.Log

import com.samsung.android.sdk.healthdata.HealthConstants
import com.samsung.android.sdk.healthdata.HealthData
import com.samsung.android.sdk.healthdata.HealthDataObserver
import com.samsung.android.sdk.healthdata.HealthDataResolver
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthResultHolder

import java.util.Calendar
import java.util.TimeZone

class StepCountReporter(private val mStore: HealthDataStore) {
    private lateinit var mStepCountObserver: (Int) -> Unit

    private
    val startTimeOfToday: Long
        get() {
            val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            return today.timeInMillis
        }

    private val mListener = { readResult: ReadResult ->
        var count = 0

        readResult.use { result ->
            for (data in result) {
                count += data.getInt(HealthConstants.StepCount.COUNT)
            }
        }
        mStepCountObserver(count)
    }

    private val mObserver = object : HealthDataObserver(null) {

        // Update the step count when a change event is received
        override fun onChange(dataTypeName: String) {
            Log.d(TAG, "Observer receives a data changed event")
            readTodayStepCount()
        }
    }

    fun start(listener: (Int) -> Unit) {
        mStepCountObserver = listener
        // Register an observer to listen changes of step count and get today step count
        HealthDataObserver.addObserver(mStore, HealthConstants.StepCount.HEALTH_DATA_TYPE, mObserver)
        readTodayStepCount()
    }

    // Read the today's step count on demand
    private fun readTodayStepCount() {
        val resolver = HealthDataResolver(mStore, null)

        // Set time range from start time of today to the current time
        val startTime = startTimeOfToday
        val endTime = startTime + ONE_DAY_IN_MILLIS

        val request = ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(arrayOf(HealthConstants.StepCount.COUNT))
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET,
                        startTime, endTime)
                .build()

        try {
            resolver.read(request).setResultListener(mListener)
        } catch (e: Exception) {
            Log.e(TAG, "Getting step count fails.", e)
        }
    }

    companion object {
        private val TAG = "Samsung${StepCountReporter::class.java.simpleName}"
        private val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    }
}
