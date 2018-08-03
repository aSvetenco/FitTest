package com.sa.healthtest.services.googleFit

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA
import com.google.android.gms.fitness.data.Field
import com.sa.healthtest.R
import com.sa.healthtest.data.model.FitResponse
import com.sa.healthtest.services.ConnectCallback
import com.sa.healthtest.services.FitConnection

class GoogleFitConnectService(private val activity: Activity) : FitConnection {

    companion object {
        val TAG = GoogleFitConnectService::class.java.simpleName
    }

    private val callback = activity as ConnectCallback
    private var account: GoogleSignInAccount? = null

    override fun connect() {
        account = GoogleSignIn.getLastSignedInAccount(activity)
        if (account == null) {
            requestUserAccount(activity)
            return
        }
        checkPermission()
    }

    override fun disconnect() {
        Fitness.getRecordingClient(activity, account)
                .unsubscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { callback.disconnected(this) }
                .addOnFailureListener { callback.error(it.message) }
    }

    fun setAccount(data: Intent?) {
        try {
            this.account = retrieveAccountFromIntent(data)
            checkPermission()
        } catch (e: ApiException) {
            callback.error(e.message)
        }
    }

    private fun checkPermission() {
        when {
            GoogleSignIn.hasPermissions(account, createFitnessOption()) -> subscribe()
            else -> GoogleSignIn.requestPermissions(
                    activity,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    createFitnessOption())
        }
    }

    private fun subscribe() {
        Fitness.getRecordingClient(activity, account)
                .subscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener {
                    callback.successConnected(this)
                    retrieveData()
                }
                .addOnFailureListener { e -> callback.error(e.message) }
    }

    private fun retrieveData() {
        //Gets daily steps count
        Fitness.getHistoryClient(activity, account)
                .readDailyTotal(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener {
                    callback.updateFitData(FitResponse(
                            clazzName = TAG,
                            stepCount = when {it.dataPoints.isEmpty() -> 0
                                else -> it.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()
                            },
                            icon = R.drawable.ic_google_fit,
                            isConnected = true))
                }
                .addOnFailureListener { callback.error(it.message) }
    }

    private fun createFitnessOption() =
            FitnessOptions.builder()
                    .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .build()
}

const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9991
const val GOOGLE_ACCOUNT_CODE = 9992

fun requestUserAccount(activity: Activity) {
    val client = GoogleSignIn.getClient(activity,
            GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build())
    val signInIntent = client.signInIntent
    activity.startActivityForResult(signInIntent, GOOGLE_ACCOUNT_CODE)
}

@Throws(ApiException::class)
fun retrieveAccountFromIntent(intent: Intent?): GoogleSignInAccount = GoogleSignIn
        .getSignedInAccountFromIntent(intent)
        .getResult(ApiException::class.java)
