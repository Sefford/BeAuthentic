/*
 * Copyright (C) 2016 Saúl Díaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sefford.beauthentic.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Adapter that abstracts a few functionalities which depend of the Google Api Client
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class GoogleApiAdapter implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final int GOOGLE_SIGN_IN = 0x10;
    public static final int REGISTER_CREDENTIAL = 0x11;
    public static final int RETRIEVE_CREDENTIALS = 0x12;

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

    public void connect(GoogleApiClient.ConnectionCallbacks connectionCallbacks) {
        if (client != null) {
            client.connect();
            client.registerConnectionCallbacks(connectionCallbacks);
            client.registerConnectionFailedListener(this);
        }
    }

    public void disconnect(GoogleApiAdapter connectionCallbacks) {
        if (client != null) {
            client.disconnect();
            client.unregisterConnectionCallbacks(connectionCallbacks);
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


    public void requestCredentials(ResultCallback<? super CredentialRequestResult> callback) {
        Auth.CredentialsApi.request(client, new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build()).setResultCallback(callback);
    }

    public void saveCredential(Credential credential, ResultCallback<? super Status> callback) {
        if (isClientAvailable()) {
            Auth.CredentialsApi.save(client, credential).setResultCallback(callback);
        }
    }

    public void removeCallbacks(GoogleApiClient.ConnectionCallbacks callback) {
        if (client != null) {
            client.unregisterConnectionCallbacks(callback);
        }
    }
}
