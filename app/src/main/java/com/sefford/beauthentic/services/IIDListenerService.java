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
import android.content.Intent;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sefford.beauthentic.callbacks.ValueEventListenerAdapter;
import com.sefford.beauthentic.utils.Constants;
import com.sefford.beauthentic.utils.Hasher;
import com.sefford.beauthentic.utils.Sessions;

import java.util.Arrays;
import java.util.List;


/**
 * Service to refresh the Instance ID token for GCM connections
 */
public class IIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

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
    // [END refresh_token]
}