package com.sefford.beauthentic.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sefford.beauthentic.auth.AuthenticAuthenticator;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
public class AuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    AuthenticAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new AuthenticAuthenticator(getApplicationContext());
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}