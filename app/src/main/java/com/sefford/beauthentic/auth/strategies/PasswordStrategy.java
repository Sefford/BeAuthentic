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
