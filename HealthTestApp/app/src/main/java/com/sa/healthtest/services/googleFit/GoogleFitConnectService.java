package com.sa.healthtest.services.googleFit;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.sa.healthtest.R;
import com.sa.healthtest.data.model.FitResponse;
import com.sa.healthtest.services.ConnectCallback;
import com.sa.healthtest.services.FitConnection;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;

public class GoogleFitConnectService implements FitConnection {

    public static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 9991;
    public static final int SIGN_IN_ACCOUNT_CODE = 9992;
    public static final String TAG = "GoogleFit";
    private GoogleSignInAccount account;
    private GoogleAccountManager accountManager;
    private final Activity activity;
    private final ConnectCallback callback;

    public GoogleFitConnectService(Activity activity, GoogleAccountManager accountManager) {
        this.activity = activity;
        this.accountManager = accountManager;
        this.callback = (ConnectCallback) activity;
        subscribeToAccountRetriever();
    }

    @Override
    public void connect() {
        if (account == null) {
            accountManager.retrieveSignInAccount();
            return;
        }
        checkPermissions();
    }

    @Override
    public void disconnect() {
        Fitness.getRecordingClient(activity, account)
                .unsubscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> callback.disconnected(this))
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    private void subscribeToAccountRetriever() {
        accountManager.getAccount()
                .subscribe(googleSignInAccount -> {
                            account = googleSignInAccount;
                            checkPermissions();
                        },
                        throwable -> callback.error(throwable.getMessage()));
    }

    private void checkPermissions() {
        FitnessOptions fitnessOptions = createFitnessOption();
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    fitnessOptions);
        } else {
            subscribe();
        }
    }

    private void subscribe() {
        Fitness.getRecordingClient(activity, account)
                .subscribe(TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> {
                    callback.successConnected(this);
                    retrieveData();
                })
                .addOnFailureListener(e -> callback.error(e.getMessage()));
    }

    private void retrieveData() {
        //Gets daily steps count
        Fitness.getHistoryClient(activity, account)
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
