package com.sa.healthtest.services.samsungHealth

import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.rule.ActivityTestRule
import com.sa.healthtest.dashboard.DashboardActivity
import com.samsung.android.sdk.healthdata.HealthDataStore
import junit.framework.Assert.assertNotNull
import org.junit.Rule


@RunWith(AndroidJUnit4::class)
class SamsungHealthServiceTest {
    @Rule
    var mainActivityActivityTestRule = ActivityTestRule<DashboardActivity>(DashboardActivity::class.java)
    private var dashboardActivity: DashboardActivity? = null
    private lateinit var mHealthDataStore: HealthDataStore


    @Before
    fun setUp() {
        dashboardActivity = mainActivityActivityTestRule.activity
        mHealthDataStore
    }

    @After
    fun tearDown() {
    }

    @Test
    fun connect() {
        assertNotNull(dashboardActivity)
    }

    @Test
    fun disconnect() {
    }

    @Test
    fun onConnected() {
    }

    @Test
    fun onConnectionFailed() {
    }

    @Test
    fun onDisconnected() {
    }
}