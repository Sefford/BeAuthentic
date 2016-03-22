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
package com.sefford.beauthentic.auth.strategies;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.auth.AuthenticAuthenticator.Strategy;

/**
 * Created by sefford on 22/03/16.
 */
public class PasswordStrategy implements Strategy {
    public static final String EXPECTED_USERNAME = "jtkirk";
    public static final String EXPECTED_PASSWORD = "kmaru";

    private static final String AUTH_TOKEN = "enterprise";

    @Override
    public Bundle confirmCredential(AccountAuthenticatorResponse response, Account account, Bundle data) {
        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, AuthenticAuthenticator.ACCOUNT_TYPE);
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT,
                EXPECTED_USERNAME.equals(account.name) &&
                        EXPECTED_PASSWORD.equals(data.getString(AuthenticAuthenticator.EXTRA_PASSWORD)));
        bundle.putString(AccountManager.KEY_AUTHTOKEN, AUTH_TOKEN);
        return bundle;
    }

    @Override
    public boolean validatePassword(String password) {
        return !TextUtils.isEmpty(password);
    }
}
