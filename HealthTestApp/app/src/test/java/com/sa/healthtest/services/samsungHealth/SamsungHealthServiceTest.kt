package com.sa.healthtest.services.samsungHealth

import com.sa.healthtest.R
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class SamsungHealthServiceTestUnit {
    private val PLATFORM_NOT_INSTALLED = HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED
    private val OLD_VERSION_PLATFORM = HealthConnectionErrorResult.OLD_VERSION_PLATFORM
    private val PLATFORM_DISABLED = HealthConnectionErrorResult.PLATFORM_DISABLED
    private val CONNECTION_FAILURE = HealthConnectionErrorResult.CONNECTION_FAILURE

    private lateinit var mSamsungHealthService: SamsungHealthService
    @Before
    fun setUp() {
        mSamsungHealthService = SamsungHealthService()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun errorMessageByErrorCode() {
        val platformNoTInstalledStringId = mSamsungHealthService.errorMessageByErrorCode(PLATFORM_NOT_INSTALLED)
        assertThat(platformNoTInstalledStringId, `is`(R.string.req_install))

        val oldVersionStringId = mSamsungHealthService.errorMessageByErrorCode(OLD_VERSION_PLATFORM)
        assertThat(oldVersionStringId, `is`(R.string.req_update))

        val platfomDisableStringId = mSamsungHealthService.errorMessageByErrorCode(PLATFORM_DISABLED)
        assertThat(platfomDisableStringId, `is`(R.string.req_enable))

        val elseCaseStringId = mSamsungHealthService.errorMessageByErrorCode(CONNECTION_FAILURE)
        assertThat(elseCaseStringId, `is`(R.string.req_availability))
    }
}