/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sefford.beauthentic.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.sefford.beauthentic.callbacks.ValueEventListenerAdapter;
import com.sefford.beauthentic.utils.Constants;
import com.sefford.beauthentic.utils.GCMUtils;
import com.sefford.beauthentic.utils.Hasher;
import com.sefford.beauthentic.utils.Sessions;

import java.util.Arrays;
import java.util.List;

/**
 * Service that refreshes the registration to the GCM server and uploads the device token into the
 * firebase database.
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // [START register_for_gcm]
        // Initially this call goes out to the network to retrieve the token, subsequent calls
        // are local.
        // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
        // See https://developers.google.com/cloud-messaging/android/start for details on this file.
        // [START get_token]
        final String token = GCMUtils.getGCMToken();
        // [END get_token]
        Log.i(TAG, "GCM Registration Token: " + token);

        if (!TextUtils.isEmpty(token)) {
            sendRegistrationToServer(token);
            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } else {
            // [END register_for_gcm]
            Log.d(TAG, "Failed to complete token refresh");
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Constants.REGISTRATION_COMPLETED_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token) {
        final Account primaryAccount = Sessions.getPrimaryPhoneAccount(AccountManager.get(getApplicationContext()));
        if (primaryAccount != null) {
            final Firebase firebase = new Firebase(Constants.FIREBASE_USER_URL + Hasher.hash(primaryAccount.name));
            final Firebase devices = firebase.child("devices");
            devices.addListenerForSingleValueEvent(new ValueEventListenerAdapter() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        devices.setValue(Arrays.asList(token));
                    } else {
                        List<String> firebaseDevices = (List<String>) snapshot.getValue();
                        if (!firebaseDevices.contains(token)) {
                            firebaseDevices.add(token);
                            devices.setValue(firebaseDevices);
                        }
                    }
                }
            });
        }
    }
}