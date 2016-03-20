package com.sefford.beauthentic.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sefford.beauthentic.activities.LoginActivity;

/**
 * Created by sefford on 20/03/16.
 */
public class AuthenticAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = "com.sefford.beauthentic";
    public static final String EXPECTED_USERNAME = "jtkirk";
    public static final String EXPECTED_PASSWORD = "kmaru";
    private static final String AUTH_TOKEN = "enterprise";

    public static final String EXTRA_PASSWORD = "extra_password";

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
        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, EXPECTED_USERNAME.equals(account.name) && EXPECTED_PASSWORD.equals(options.getString(EXTRA_PASSWORD)));
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
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
}
