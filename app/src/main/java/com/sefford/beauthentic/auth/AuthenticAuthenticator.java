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
package com.sefford.beauthentic.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.sefford.beauthentic.activities.LoginActivity;
import com.sefford.beauthentic.auth.strategies.GoogleStrategy;
import com.sefford.beauthentic.auth.strategies.PasswordStrategy;

/**
 * Created by sefford on 20/03/16.
 */
public class AuthenticAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = "com.sefford.beauthentic";
    public static final String AUTHTOKEN_TYPE = "beauthentic";

    public static final String EXTRA_PASSWORD = "extra_password";
    public static final String EXTRA_AUTH = "extra_auth";
    public static final String EXTRA_TYPE = "extra_type";

    final Context context;

    public AuthenticAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    // Launches the Authentication Activity (LoginActivity in this example)
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle data = new Bundle();
        data.putParcelable(AccountManager.KEY_INTENT, intent);
        return data;
    }

    // Actually performs the Login
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return selectLoginStrategy(Type.values()[options.getInt(EXTRA_TYPE)]).confirmCredential(response, account, options);
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        final AccountManager am = AccountManager.get(context);
        String authToken = am.peekAuthToken(account, authTokenType);

        // The token has been invalidated but we have the password
        final Strategy strategy = selectLoginStrategy(Type.values()[options.getInt(EXTRA_TYPE)]);
        if (TextUtils.isEmpty(authToken) && strategy.validatePassword(am.getPassword(account))) {
            final Bundle data = new Bundle();
            data.putInt(EXTRA_TYPE, options.getInt(EXTRA_TYPE));
            data.putString(EXTRA_PASSWORD, am.getPassword(account));

            // In this case we try to re-sign in to obtain the authtoken
            final Bundle results = confirmCredentials(response, account, data);

            // If we succeeded we refresh the token and return the results
            if (results.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                am.setAuthToken(account, authTokenType, results.getString(AccountManager.KEY_AUTHTOKEN));
                return results;
            } else {
                // Otherwise seems like username or password changed and we need to re-input credentials
                return addAccount(response, ACCOUNT_TYPE, authTokenType, null, options);
            }
        } else if (TextUtils.isEmpty(authToken) && !strategy.validatePassword(am.getPassword(account))) {
            // If we do not have the password, we cannot re-log in and we need to re-input credentials
            return addAccount(response, ACCOUNT_TYPE, AuthenticAuthenticator.AUTHTOKEN_TYPE, null, options);
        }

        // If the invalidation performs properly, then we're good to go
        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    Strategy selectLoginStrategy(Type type) {
        switch (type) {
            case PASSWORD:
                return new PasswordStrategy();
            case GOOGLE:
            default:
                return new GoogleStrategy();
        }
    }

    public enum Type {
        PASSWORD,
        GOOGLE
    }


    public interface Strategy {

        Bundle confirmCredential(AccountAuthenticatorResponse response, Account account, Bundle data);

        boolean validatePassword(String password);

    }
}
