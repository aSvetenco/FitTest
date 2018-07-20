package com.sa.healthtest

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.sa.healthtest.connect.ConnectCallback
import com.sa.healthtest.connect.GoogleFitConnectService
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity(), ConnectCallback {

    override fun setFitData(data: MutableList<DataSet>?) {

    }

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9991
    private lateinit var service: GoogleFitConnectService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        service = GoogleFitConnectService(this)
        google_fit.setOnClickListener { service.connectToGoogleFit()}
        retrieve.setOnClickListener{service.retrieveData()}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                service.startRecordData()
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
            service.startRecordData()
        }

    }

    override fun successConnected() {
        google_fit.text = getString(R.string.connected)
        google_fit.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.ic_check),null, null, null)
    }

    override fun error() {

    }


}
