package com.sa.healthtest.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.fitness.FitnessOptions
import com.sa.healthtest.R
import com.sa.healthtest.dashboard.list.ResultsRVAdapter
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.GoogleFitConnectService
import com.sa.healthtest.dashboard.list.ServiceRVAdapter
import com.sa.healthtest.data.SharedPref
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.services.FitConnection
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.nav_menu_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class DashboardActivity : AppCompatActivity(), ConnectCallback {

    override fun setFitData(steps: FitResponse) {
        tvAlert.visibility = View.GONE
        results.visibility = View.VISIBLE
        resultAdapter.setData(steps)
    }

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9991
    private lateinit var googleService: GoogleFitConnectService
    private lateinit var navMenu: DrawerLayout
    private lateinit var preferences: SharedPref
    private lateinit var tvAlert: TextView
    private val serviceAdapter = ServiceRVAdapter()
    private val resultAdapter = ResultsRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        tvAlert = findViewById(R.id.alert)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        navMenu = findViewById(R.id.nav_menu)
        initNavDrawer()
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = ""
        }
        initSharedPreferences()
        googleService = GoogleFitConnectService(this)
        results.layoutManager = LinearLayoutManager(this)
        results.adapter = resultAdapter
        getResults(listOf(googleService))
        mapAndFillServiceList()

    }

    private fun getResults(services: List<FitConnection>) {
        var atLeastOnActive = false
        services.forEach {
            if (preferences.isConnected(it.javaClass.simpleName)) {
                it.retrieveData()
                atLeastOnActive = true
            }
        }
        if (!atLeastOnActive) {
            results.visibility = View.GONE
            tvAlert.visibility = View.VISIBLE
            tvAlert.text = getString(R.string.no_connected_yet)
        }
    }

    private fun initSharedPreferences() {
        val sharedPreferences = getSharedPreferences("health app preferences", Context.MODE_PRIVATE)
        preferences = SharedPref(sharedPreferences)
    }

    private fun mapAndFillServiceList() {
        val serviceList: List<FitResponse> = listOf(
                FitResponse(GoogleFitConnectService.TAG, 0, R.drawable.ic_google_fit, false),
                FitResponse("SamsungHealth", 0, R.drawable.ic_samsung_fit, false))
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
        when (data.isConnected) {
            true -> googleService.checkPermission()
            false -> googleService.disconnect()
        }
    }

    private fun handleSamsungConnection(data: FitResponse) {

    }

    private fun initNavDrawer() {
        val toggle = ActionBarDrawerToggle(this, navMenu, toolbar, R.string.nav_open, R.string.nav_close)
        toggle.apply {
            isDrawerIndicatorEnabled = true
            setToolbarNavigationClickListener { navMenu.openDrawer(GravityCompat.START) }
        }
        navMenu.addDrawerListener(toggle)
        toggle.syncState()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                googleService.connect()
            }
        }
    }

    override fun checkPermissions(options: FitnessOptions) {
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), options)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    options)
        } else {
            googleService.connect()
        }

    }

    override fun successConnected(service: FitConnection) {
        preferences.setConnected(service.javaClass.simpleName, true)
        service.retrieveData()
    }

    override fun error(message: String) {
        Toast.makeText(this, "Error message: $message", Toast.LENGTH_LONG).show()
    }
}
