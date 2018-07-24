package com.sa.healthtest.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.sa.healthtest.R
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.GoogleFitConnectService
import com.sa.healthtest.dashboard.list.ServiceRVAdapter
import com.sa.healthtest.data.SharedPref
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.services.FitConnection
import kotlinx.android.synthetic.main.nav_menu_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class DashboardActivity : AppCompatActivity(), ConnectCallback {

    override fun setFitData(steps: Int) {

    }

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9991
    private lateinit var service: GoogleFitConnectService
    private lateinit var navMenu: DrawerLayout
    private lateinit var preferences: SharedPref
    private val serviceAdapter = ServiceRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
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
        service = GoogleFitConnectService(this)
        mapAndFillService()

    }

    private fun initSharedPreferences() {
        val sharedPreferences = getSharedPreferences("health app preferences", Context.MODE_PRIVATE)
        preferences = SharedPref(sharedPreferences)
    }

    private fun mapAndFillService() {
        val serviceList: List<FitResponse> = listOf(
                FitResponse("GoogleFit", 0, R.drawable.ic_google_fit, false),
                FitResponse("SamsungHealth", 0, R.drawable.ic_samsung_fit, false))
        services.layoutManager = LinearLayoutManager(this)
        services.adapter = serviceAdapter
        serviceAdapter.setData(serviceList)
        connectionChangeListener()
    }

    private fun checkConnectionToFitService() {

    }


    private fun connectionChangeListener() {
        serviceAdapter.onSwitchStateChangedListener()
                .doOnNext {
                    if (it.resourceName == "GoogleFit") {
                        handleGoogleConnection(it)
                    } else handleSamsungConnection(it)
                }
                .subscribe {}
    }

    private fun handleGoogleConnection(data: FitResponse) {
       when (data.isConnected){
           true -> service.checkPermission()
           false -> service.disconnect()
       }
    }

    private fun handleSamsungConnection(data: FitResponse) {

    }

    private fun initNavDrawer() {
        val toggle = object : ActionBarDrawerToggle(this, navMenu, toolbar, R.string.nav_open, R.string.nav_close) {
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)

            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

            }
        }
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
                service.connect()
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
            service.connect()
        }

    }

    override fun successConnected(service: FitConnection) {
        preferences.setConnected(service.javaClass.simpleName, true)
        service.retrieveData()
    }

    private fun updateData() {

    }

    override fun error(message: String) {

    }


}
