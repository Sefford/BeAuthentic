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
package com.sefford.beauthentic.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.sefford.beauthentic.R;
import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.callbacks.TextWatcherAdapter;
import com.sefford.beauthentic.callbacks.ValueEventListenerAdapter;
import com.sefford.beauthentic.providers.MessageProvider;
import com.sefford.beauthentic.services.LoginGCMNotificationService;
import com.sefford.beauthentic.services.NotifySyncService;
import com.sefford.beauthentic.utils.Constants;
import com.sefford.beauthentic.utils.GoogleApiAdapter;
import com.sefford.beauthentic.utils.Hasher;
import com.sefford.beauthentic.utils.Sessions;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Seconday activity of the app, most of the show happens here.
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class LoggedActivity extends AppCompatActivity implements Handler.Callback {

    public static final String ACTION_REFRESH = "com.sefford.beauthentic.REFRESH";
    public static final String EXTRA_MESSAGE = "extra_message";

    static final int MSG_SYNC = 0x100;

    @Bind(R.id.tv_status)
    TextView tvStatus;
    @Bind(R.id.et_syncable)
    EditText etSyncable;

    Handler handler;

    AccountManager am;
    GoogleApiAdapter googleApi = GoogleApiAdapter.getInstance();
    TextWatcherAdapter watcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            if (handler != null) {
                handler.removeMessages(MSG_SYNC);
                handler.sendEmptyMessageDelayed(MSG_SYNC, 600);
            }
        }
    };

    BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (etSyncable != null) {
                etSyncable.removeTextChangedListener(watcher);
                etSyncable.setText(intent.getStringExtra(EXTRA_MESSAGE));
                etSyncable.addTextChangedListener(watcher);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_logged);
        am = AccountManager.get(getApplicationContext());
        ButterKnife.bind(this);
        googleApi.connect(googleApi);
        handler = new Handler(this);
        etSyncable.addTextChangedListener(watcher);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REFRESH);
        registerReceiver(updateReceiver, intentFilter);
        ContentResolver.requestSync(Sessions.getAccount(am), MessageProvider.AUTHORITY, Bundle.EMPTY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Sessions.isLogged(am)) {
            onLoggingOut();
        } else {
            getSupportActionBar().setTitle(Sessions.getAccount(am).name);
        }
    }

    private void logout() {
        finish();
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.bt_invalidate)
    public void onAuthTokenInvalidated() {
        if (!TextUtils.isEmpty(am.peekAuthToken(Sessions.getAccount(am), AuthenticAuthenticator.AUTHTOKEN_TYPE))) {
            am.invalidateAuthToken(AuthenticAuthenticator.ACCOUNT_TYPE, am.peekAuthToken(Sessions.getAccount(am), AuthenticAuthenticator.AUTHTOKEN_TYPE));
            tvStatus.setText(R.string.status_invalidated);
        }
    }

    @OnClick(R.id.bt_delete_pass)
    public void onDeletePassword() {
        am.clearPassword(Sessions.getAccount(am));
        tvStatus.setText(R.string.status_forgot);
    }

    @OnClick(R.id.bt_refresh)
    public void onGettingAuthToken() {
        if (Sessions.isLogged(am)) {
            final Bundle data = new Bundle();
            data.putInt(AuthenticAuthenticator.EXTRA_TYPE, Integer.valueOf(am.getUserData(Sessions.getAccount(am), AuthenticAuthenticator.EXTRA_TYPE)));
            am.getAuthToken(Sessions.getAccount(am), AuthenticAuthenticator.AUTHTOKEN_TYPE, data, true, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        final Bundle result = future.getResult();
                        if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                            tvStatus.setText(R.string.status_logged);
                        } else {
                            // We would be returning a intent here to open Login Activity, so we do not care, actually
                            onLoggingOut();
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
        } else {
            onLoggingOut();
        }
    }

    @OnClick(R.id.bt_logout)
    public void onLoggingOut() {
        am.removeAccount(Sessions.getAccount(am), new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                logout();
            }
        }, null);
    }

    @Override
    public boolean handleMessage(Message msg) {
        final Account primaryAccount = Sessions.getPrimaryPhoneAccount(AccountManager.get(getApplicationContext()));
        if (primaryAccount != null) {
            final Firebase firebase = new Firebase(Constants.FIREBASE_USER_URL + Hasher.hash(primaryAccount.name));
            final Firebase message = firebase.child("message");
            message.setValue(etSyncable.getText().toString());
            orderSync();
        }
        return false;
    }

    void orderSync() {
        final Account primaryAccount = Sessions.getPrimaryPhoneAccount(AccountManager.get(getApplicationContext()));
        if (primaryAccount == null) {
            return;
        }
        final Firebase firebase = new Firebase(Constants.FIREBASE_USER_URL + Hasher.hash(primaryAccount.name));
        final Firebase devices = firebase.child("devices");
        if (primaryAccount != null) {
            devices.addListenerForSingleValueEvent(new ValueEventListenerAdapter() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Intent intent = new Intent(LoggedActivity.this, NotifySyncService.class);
                        intent.putStringArrayListExtra(LoginGCMNotificationService.EXTRA_DEVICES, (ArrayList<String>) snapshot.getValue());
                        startService(intent);
                    }
                }
            });
        }
    }
}
