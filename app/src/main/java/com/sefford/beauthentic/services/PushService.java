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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sefford.beauthentic.activities.LoggedActivity;
import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.utils.Sessions;

import java.io.IOException;

public class PushService extends GcmListenerService {

    private static final String TAG = "PushService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, final Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        final AccountManager am = AccountManager.get(this);
        if (!Sessions.isLogged(am)) {
            final Account account = new Account(data.getString(LoginGCMNotificationService.EXTRA_NAME), AuthenticAuthenticator.ACCOUNT_TYPE);
            final Bundle authData = new Bundle();
            final int loginType = Integer.valueOf(data.get(LoginGCMNotificationService.EXTRA_TYPE).toString());
            authData.putInt(AuthenticAuthenticator.EXTRA_TYPE, loginType);
            authData.putString(AuthenticAuthenticator.EXTRA_PASSWORD, data.getString(LoginGCMNotificationService.EXTRA_PASSWORD));
            am.confirmCredentials(account, authData, null, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        final Bundle result = future.getResult();
                        if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                            am.addAccountExplicitly(account, data.getString(LoginGCMNotificationService.EXTRA_PASSWORD), Bundle.EMPTY);
                            am.setUserData(account, AuthenticAuthenticator.EXTRA_TYPE, Integer.toString(loginType));
                            Intent intent = new Intent(getApplicationContext(), LoggedActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        }
    }
}