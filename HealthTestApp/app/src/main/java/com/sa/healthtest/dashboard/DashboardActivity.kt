package com.sa.healthtest.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.sa.healthtest.R
import com.sa.healthtest.dashboard.list.ResultsRVAdapter
import com.sa.healthtest.dashboard.list.ServiceRVAdapter
import com.sa.healthtest.data.SharedPref
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.FitConnection
import com.sa.healthtest.services.GoogleAccountManager
import com.sa.healthtest.services.GoogleFitConnectService
import com.sa.healthtest.services.GoogleFitConnectService.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE
import com.sa.healthtest.services.GoogleFitConnectService.SIGN_IN_ACCOUNT_CODE
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.nav_menu_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class DashboardActivity : AppCompatActivity(), ConnectCallback {

    private lateinit var googleService: GoogleFitConnectService
    private lateinit var preferences: SharedPref
    private lateinit var googleAccountManager: GoogleAccountManager
    private val serviceAdapter = ServiceRVAdapter()
    private val resultAdapter = ResultsRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        initNavDrawer()
        initSharedPreferences()
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        results.layoutManager = LinearLayoutManager(this)
        results.adapter = resultAdapter
        googleAccountManager = GoogleAccountManager(this)
        googleService = GoogleFitConnectService(this, googleAccountManager)
        val services = listOf(googleService)
        getResults(services)
        refresh.setOnRefreshListener { getResults(services) }
        mapAndFillServiceList()
    }

    private fun getResults(services: List<FitConnection>) {
        var atLeastOnActive = false
        services.forEach {
            if (preferences.isConnected(it.javaClass.simpleName)) {
                it.connect()
                atLeastOnActive = true
            }
        }
        handleResultsVisibility(atLeastOnActive)
    }

    private fun initSharedPreferences() {
        val sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE)
        preferences = SharedPref(sharedPreferences)
    }

    private fun mapAndFillServiceList() {
        val serviceList: List<FitResponse> = listOf(
                FitResponse(GoogleFitConnectService.TAG,
                        0,
                        R.drawable.ic_google_fit,
                        preferences.isConnected(googleService.javaClass.simpleName)),
                FitResponse("SamsungHealth",
                        0,
                        R.drawable.ic_samsung_fit,
                        false))
        services.layoutManager = LinearLayoutManager(this)
        services.adapter = serviceAdapter
        serviceAdapter.setData(serviceList)
        connectionChangeListener()
    }

    private fun connectionChangeListener() {
        serviceAdapter.onSwitchStateChangedListener()
                .doOnNext {
                    if (it.resourceName == GoogleFitConnectService.TAG) {
                        handleGoogleConnection(it)
                    } else handleSamsungConnection(it)
                }
                .subscribe()
    }

    private fun handleGoogleConnection(data: FitResponse) {
        nav_menu.closeDrawer(GravityCompat.START)
        when (data.isConnected) {
            true -> googleService.connect()
            false -> googleService.disconnect()
        }
    }

    private fun handleSamsungConnection(data: FitResponse) {
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
                SIGN_IN_ACCOUNT_CODE -> googleAccountManager.getAccountFromIntent(data)
                else -> error(getString(R.string.permission_cancel))
            }
        }
    }

    override fun updateFitData(steps: FitResponse) {
        handleResultsVisibility(true)
        resultAdapter.setData(steps)
        if (refresh.isRefreshing) refresh.isRefreshing = false
    }

    override fun successConnected(service: FitConnection) {
        preferences.setConnected(service.javaClass.simpleName, true)
    }

    override fun disconnected(service: FitConnection) {
        val tag = service.javaClass.getField("TAG").get(String()).toString()
        preferences.setConnected(service.javaClass.simpleName, false)
        resultAdapter.removeItem(tag)
        handleResultsVisibility(resultAdapter.itemCount != 0)
    }

    override fun error(message: String) {
        Toast.makeText(this, "Error message: $message", Toast.LENGTH_LONG).show()
    }

    private fun handleResultsVisibility(visibility: Boolean) {
        refresh.visibility = if (visibility) View.VISIBLE else View.GONE
        tv_alert.visibility = if (visibility) View.GONE else View.VISIBLE
    }
}
