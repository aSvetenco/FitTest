package com.sa.healthtest.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.sa.healthtest.R
import com.sa.healthtest.dashboard.list.ResultsRVAdapter
import com.sa.healthtest.dashboard.list.ServiceRVAdapter
import com.sa.healthtest.data.isServiceConnected
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.data.setServiceConnected
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.FitConnection
import com.sa.healthtest.services.googleFit.GOOGLE_ACCOUNT_CODE
import com.sa.healthtest.services.googleFit.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE
import com.sa.healthtest.services.googleFit.GoogleFitConnectService
import com.sa.healthtest.services.samsungHealth.SamsungHealthService
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.nav_menu_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class DashboardActivity : AppCompatActivity(), ConnectCallback {

    private val TAG = DashboardActivity::class.java.simpleName
    private lateinit var googleService: GoogleFitConnectService
    private lateinit var samsungService: SamsungHealthService
    private lateinit var preferences: SharedPreferences
    private val serviceAdapter = ServiceRVAdapter()
    private val resultAdapter = ResultsRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        initNavDrawer()
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        results.layoutManager = LinearLayoutManager(this)
        results.adapter = resultAdapter
        preferences = getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
        googleService = GoogleFitConnectService(this)
        samsungService = SamsungHealthService(this)
        val services = listOf(googleService, samsungService)
        getResults(services)
        refresh.setOnRefreshListener { getResults(services) }
        mapAndFillServiceList()
    }

    private fun getResults(services: List<FitConnection>) {
        var atLeastOnActive = false
        services.forEach {
            if (preferences.isServiceConnected(it::class.java.simpleName)) {
                it.connect()
                atLeastOnActive = true
            }
        }
        handleResultsVisibility(atLeastOnActive)
    }

    private fun mapAndFillServiceList() {
        val serviceList: List<FitResponse> = listOf(
                FitResponse(googleService::class.java.simpleName,
                        getString(R.string.google_fit),
                        icon = R.drawable.ic_google_fit,
                        isConnected = preferences.isServiceConnected(googleService.javaClass.simpleName)),
                FitResponse(samsungService::class.java.simpleName,
                        getString(R.string.samsung_health),
                        icon = R.drawable.ic_samsung_fit,
                        isConnected = preferences.isServiceConnected(samsungService::class.java.simpleName)))
        services.layoutManager = LinearLayoutManager(this)
        services.adapter = serviceAdapter
        serviceAdapter.setData(serviceList)
        connectionChangeListener()
    }

    private fun connectionChangeListener() {
        serviceAdapter.switchListener = { response ->
            if (response.resourceName == getString(R.string.google_fit)) {
                handleGoogleConnection(response)
            } else handleSamsungConnection(response)
        }
    }

    private fun handleGoogleConnection(data: FitResponse) {
        nav_menu.closeDrawer(GravityCompat.START)
        when (data.isConnected) {
            true -> googleService.connect()
            false -> googleService.disconnect()
        }
    }

    private fun handleSamsungConnection(data: FitResponse) {
        nav_menu.closeDrawer(GravityCompat.START)
        when (data.isConnected) {
            true -> samsungService.connect()
            false -> samsungService.disconnect()
        }
    }

    private fun initNavDrawer() {
        val toggle = ActionBarDrawerToggle(this, nav_menu, toolbar, R.string.nav_open, R.string.nav_close)
        toggle.apply {
            isDrawerIndicatorEnabled = true
            setToolbarNavigationClickListener { nav_menu.openDrawer(GravityCompat.START) }
        }
        nav_menu.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> googleService.connect()
                GOOGLE_ACCOUNT_CODE -> googleService.setAccount(data)
                else -> {
                    onPermissionDenied(googleService)
                    error(getString(R.string.permission_cancel))
                }
            }
        }
    }

    override fun updateFitData(steps: FitResponse) {
        handleResultsVisibility(true)
        resultAdapter.setData(steps)
        if (refresh.isRefreshing) refresh.isRefreshing = false
    }

    override fun successConnected(service: FitConnection) {
        preferences.setServiceConnected(service::class.java.simpleName, true)
        handleResultsVisibility(true)
    }

    override fun disconnected(service: FitConnection) {
        preferences.setServiceConnected(service::class.java.simpleName, false)
        val tag = service::class.java.simpleName
        resultAdapter.removeItem(service::class.java.simpleName)
        handleResultsVisibility(resultAdapter.itemCount != 0)
    }

    override fun error(message: String?) {
        Toast.makeText(this, "Error message: $message", Toast.LENGTH_LONG).show()
    }

    private fun handleResultsVisibility(visibility: Boolean) {
        refresh.visibility = if (visibility) View.VISIBLE else View.GONE
        tv_alert.visibility = if (visibility) View.GONE else View.VISIBLE
    }

    override fun onPermissionDenied(service: FitConnection?) {
        if (service == null) return
        Log.d(TAG, "onPermissionDenied ${service::class.java.simpleName}")
        serviceAdapter.onUserDeniedPermission(service::class.java.simpleName)
    }
}
