package com.sa.healthtest.services.googleFit;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;

import static com.sa.healthtest.services.googleFit.GoogleFitConnectService.SIGN_IN_ACCOUNT_CODE;

public class GoogleAccountManager {

    private final Activity activity;
    private SingleSubject<GoogleSignInAccount> accountListener = SingleSubject.create();

    public GoogleAccountManager(Activity activity) {
        this.activity = activity;
    }

    public Single<GoogleSignInAccount> getAccount() {
        return accountListener;
    }

    public void getAccountFromIntent(Intent intent) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(intent).getResult(ApiException.class);
            accountListener.onSuccess(account);
        } catch (ApiException e) {
            accountListener.onError(e);
        }
    }

    public void retrieveSignInAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(activity, gso);
            Intent signInIntent = client.getSignInIntent();
            activity.startActivityForResult(signInIntent, SIGN_IN_ACCOUNT_CODE);
        } else {
            accountListener.onSuccess(account);
        }
    }

}
