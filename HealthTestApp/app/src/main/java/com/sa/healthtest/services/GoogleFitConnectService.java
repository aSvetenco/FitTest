package com.sa.healthtest.services;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sa.healthtest.R;
import com.sa.healthtest.data.model.FitResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GoogleFitConnectService implements FitConnection {

    private static final String TAG = GoogleFitConnectService.class.getSimpleName();
    private final Activity activity;
    private final ConnectCallback callback;

    public GoogleFitConnectService(Activity activity) {
        this.activity = activity;
        this.callback = (ConnectCallback) activity;
    }

    @Override
    public void checkPermission() {
        FitnessOptions fitnessOptions = createFitnessOption();
        callback.checkPermissions(fitnessOptions);
    }

    @Override
    public void disconnect() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account != null) {
            Fitness.getConfigClient(activity, account).disableFit();
        }
    }

    @Override
    public void connect() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account != null) {
            Fitness.getRecordingClient(activity, account)
                    .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(aVoid -> callback.successConnected(this))
                    .addOnFailureListener(e -> callback.error(e.getMessage()));
        } else {
            callback.error("Account doesn't exist");
        }
    }

    @Override
    public void retrieveData() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(7, TimeUnit.DAYS)
                .build();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account != null) {
            Fitness.getHistoryClient(activity, account)
                    .readData(readRequest)
                    .addOnSuccessListener(dataReadResponse -> {
                        DataSet dataSet = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        int value = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        if(value == 0) {
                            callback.error();
                        } else {
                            callback.setFitData(new FitResponse("GoogleFit", value, R.drawable.ic_google_fit, true));
                        }
                    })
                    .addOnFailureListener(e -> callback.error(e.getMessage()))
                    .addOnCompleteListener(task -> Log.d(TAG, "onComplete()"));
        }
    }


    private FitnessOptions createFitnessOption() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
    }

}
