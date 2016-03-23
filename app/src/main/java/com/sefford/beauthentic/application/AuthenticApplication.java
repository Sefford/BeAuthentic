package com.sefford.beauthentic.application;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by sefford on 23/03/16.
 */
public class AuthenticApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
