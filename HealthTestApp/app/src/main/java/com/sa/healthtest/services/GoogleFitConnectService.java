package com.sa.healthtest.services;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.sa.healthtest.R;
import com.sa.healthtest.data.model.FitResponse;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;

public class GoogleFitConnectService implements FitConnection {

    public static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9991;
    public static final String TAG = "GoogleFit";
    private final Activity activity;
    private final ConnectCallback callback;

    public GoogleFitConnectService(Activity activity) {
        this.activity = activity;
        this.callback = (ConnectCallback) activity;
    }

    @Override
    public void checkPermission() {
        FitnessOptions fitnessOptions = createFitnessOption();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    fitnessOptions);
        } else {
            connect();
        }
    }

    @Override
    public void disconnect() {
        Fitness.getConfigClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).disableFit();
        callback.disconnected(this);
    }

    @Override
    public void connect() {
        Fitness.getRecordingClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .subscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> {
                    callback.successConnected(this);
                    retrieveData();
                })
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    @Override
    public void retrieveData() {
        //Gets daily steps count
        Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .readDailyTotal(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    int value = dataSet.getDataPoints().isEmpty()
                            ? 0
                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    callback.updateFitData(new FitResponse(TAG,
                            value,
                            R.drawable.ic_google_fit,
                            true));
                })
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    private FitnessOptions createFitnessOption() {
        return FitnessOptions.builder()
                .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
    }


}
