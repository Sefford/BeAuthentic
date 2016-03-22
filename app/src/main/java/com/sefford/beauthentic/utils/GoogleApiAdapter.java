package com.sefford.beauthentic.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

/**
 * Created by sefford on 21/03/16.
 */
public class GoogleApiAdapter implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final int GOOGLE_SIGN_IN = 0x10;

    final GoogleSignInOptions gso;

    GoogleApiClient client;

    private static GoogleApiAdapter INSTANCE = new GoogleApiAdapter();

    private GoogleApiAdapter() {
        this.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("670097636358-oi7ac0j1q2vk3oa3475bfjdcd1cojsa9.apps.googleusercontent.com")
                .build();
    }

    public void initialize(Context context) {
        this.client = new GoogleApiClient.Builder(context)
                .addApi(Auth.CREDENTIALS_API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void connect() {
        if (client != null) {
            client.connect();
            client.registerConnectionCallbacks(this);
            client.registerConnectionFailedListener(this);
        }
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect();
            client.unregisterConnectionCallbacks(this);
            client.unregisterConnectionFailedListener(this);
        }
    }

    public void performGoogleSignIn(Activity activity) {
        if (isClientAvailable()) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
            activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public boolean isClientAvailable() {
        return client != null && client.isConnected();
    }

    public OptionalPendingResult<GoogleSignInResult> performSilentSignIn() {
        return Auth.GoogleSignInApi.silentSignIn(client);
    }

    public static GoogleApiAdapter getInstance() {
        return INSTANCE;
    }
}
