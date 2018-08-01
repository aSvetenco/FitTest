package com.sa.healthtest.data

import android.content.SharedPreferences
import com.sa.healthtest.services.googleFit.GoogleFitConnectService
import com.sa.healthtest.services.samsungHealth.SamsungHealthService
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString

@RunWith(MockitoJUnitRunner::class)
class SharedPrefTest {
    private val IS_NOT_CONNECTED = false
    private val IS_CONNECTED = true
    private val SAMSUNG_HEALTH = SamsungHealthService::class.java.simpleName
    private val GOOGLE_FIT = GoogleFitConnectService::class.java.simpleName
    private lateinit var mMockSharedPreferencesHelper: SharedPref

    @Mock
    private lateinit var mMockSharedPreferences: SharedPreferences
    @Mock
    private lateinit var mMockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {

        // Create a mocked SharedPreferences.
        mMockSharedPreferencesHelper = createMockSharedPreference()
    }

    @Test
    fun sharedPref_SaveAndReadIsConnected() {
        mMockSharedPreferencesHelper.setConnected(SAMSUNG_HEALTH, true)

        val isConnected = mMockSharedPreferencesHelper.isConnected(SAMSUNG_HEALTH)
        assertTrue(isConnected)
    }

    /**
     * Creates a mocked SharedPreferences.
     */
    private fun createMockSharedPreference(): SharedPref {

        // Mocking reading the SharedPreferences as if mMockSharedPreferences was previously written
        // correctly.
        `when`(mMockSharedPreferences.getBoolean(SAMSUNG_HEALTH,
                IS_NOT_CONNECTED))
                .thenReturn(IS_CONNECTED)

        // Return the MockEditor when requesting it.
        `when`(mMockSharedPreferences.edit())
                .thenReturn(mMockEditor)

        `when`(mMockSharedPreferences.edit().putBoolean(anyString(), anyBoolean()))
                .thenReturn(mMockEditor)

        return SharedPref(mMockSharedPreferences)
    }
}