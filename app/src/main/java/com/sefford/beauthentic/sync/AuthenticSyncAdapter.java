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
package com.sefford.beauthentic.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.sefford.beauthentic.activities.LoggedActivity;
import com.sefford.beauthentic.callbacks.ValueEventListenerAdapter;
import com.sefford.beauthentic.utils.Constants;
import com.sefford.beauthentic.utils.Hasher;
import com.sefford.beauthentic.utils.Sessions;

/**
 * SyncAdapter that brings the current message shared between all the devices of the same Google accounts.
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 **/
public class AuthenticSyncAdapter extends AbstractThreadedSyncAdapter {

    public AuthenticSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public AuthenticSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, final SyncResult syncResult) {
        final Account primaryAccount = Sessions.getPrimaryPhoneAccount(AccountManager.get(getContext()));
        if (primaryAccount != null) {
            final Firebase firebase = new Firebase(Constants.FIREBASE_USER_URL + Hasher.hash(primaryAccount.name));
            final Firebase message = firebase.child("message");
            message.addListenerForSingleValueEvent(new ValueEventListenerAdapter() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    final Intent intent = new Intent(LoggedActivity.ACTION_REFRESH);
                    intent.putExtra(LoggedActivity.EXTRA_MESSAGE, TextUtils.isEmpty(snapshot.getValue().toString()) ? "" : snapshot.getValue().toString());
                    getContext().sendBroadcast(intent);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    syncResult.stats.numIoExceptions++;
                }
            });
        }
        syncResult.stats.numUpdates++;
    }
}
