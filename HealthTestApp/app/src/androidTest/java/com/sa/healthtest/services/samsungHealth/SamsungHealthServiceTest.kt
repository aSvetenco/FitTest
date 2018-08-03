package com.sa.healthtest.services.samsungHealth

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.sa.healthtest.dashboard.DashboardActivity
import com.samsung.android.sdk.healthdata.HealthDataService
import com.samsung.android.sdk.healthdata.HealthDataStore
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class SamsungHealthServiceTest {

    @get:Rule
    val mDashBoardActivityRule = ActivityTestRule<DashboardActivity>(DashboardActivity::class.java)
    lateinit var mDashBoardActivity: DashboardActivity
    private var mMockListener: HealthDataStore.ConnectionListener? = null
    private var mHealthDataStore: HealthDataStore? = null

    @Before
    fun setUp() {
        mDashBoardActivity = mDashBoardActivityRule.activity
        HealthDataService().also { it.initialize(mDashBoardActivity) }
        mMockListener = Mockito.mock(HealthDataStore.ConnectionListener::class.java)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun initSdk() {
        val healthDataService = HealthDataService()
        assertNotNull(healthDataService)
        assertNotNull(mDashBoardActivity)
    }

    @Test
    fun connect() {
        mHealthDataStore = HealthDataStore(mDashBoardActivity, mMockListener)
        assertNotNull(mHealthDataStore)
    }
}
