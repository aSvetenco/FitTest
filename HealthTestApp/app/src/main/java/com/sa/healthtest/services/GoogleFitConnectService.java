package com.sa.healthtest.services;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.common.api.ApiException;
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
    private GoogleSignInAccount account;
    private final Activity activity;
    private final ConnectCallback callback;

    public GoogleFitConnectService(Activity activity) {
        this.activity = activity;
        this.callback = (ConnectCallback) activity;
    }

    @Override
    public void checkPermission() {
        getSignInAccount();
    }

    private void getSignInAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(activity, gso);
            Intent signInIntent = client.getSignInIntent();
            activity.startActivityForResult(signInIntent, 222);
        } else {
            this.account = account;
            requestPermission();
        }
    }

    private void requestPermission() {
        FitnessOptions fitnessOptions = createFitnessOption();
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

    public void getAccountFromIntent(Intent intent) {
        try {
            this.account = GoogleSignIn.getSignedInAccountFromIntent(intent).getResult(ApiException.class);
            requestPermission();
        } catch (ApiException e) {
            callback.error(e.getMessage());
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
