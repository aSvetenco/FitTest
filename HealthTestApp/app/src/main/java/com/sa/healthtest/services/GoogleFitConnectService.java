package com.sa.healthtest.services;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if(GoogleSignIn.getLastSignedInAccount(activity) == null){
            callback.error("You should signIn before connect to googleFit");
            return;
        }
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
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
        Fitness.getRecordingClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .unsubscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> callback.disconnected(this))
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    private void connect() {
        Fitness.getRecordingClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .subscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> {
                    callback.successConnected(this);
                    retrieveData();
                })
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    private void retrieveData() {
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
