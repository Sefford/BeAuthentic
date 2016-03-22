package com.sefford.beauthentic.auth.strategies;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.utils.GoogleApiAdapter;

/**
 * Created by sefford on 22/03/16.
 */
public class GoogleStrategy implements AuthenticAuthenticator.Strategy {

    public GoogleStrategy() {
    }

    @Override
    public Bundle confirmCredential(AccountAuthenticatorResponse response, Account account, Bundle data) {
        final Bundle bundle = new Bundle();
        if (TextUtils.isEmpty(data.getString(AccountManager.KEY_AUTHTOKEN))) {
            final OptionalPendingResult<GoogleSignInResult> pendingResult = GoogleApiAdapter.getInstance().performSilentSignIn();
            if (pendingResult != null) {
                final GoogleSignInResult googleSignInResult = pendingResult.get();
                if (googleSignInResult != null) {
                    final GoogleSignInAccount signInAccount = googleSignInResult.getSignInAccount();
                    if (signInAccount != null) {
                        bundle.putString(AccountManager.KEY_AUTHTOKEN, signInAccount.getIdToken());
                        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, signInAccount.getDisplayName());
                    }
                }
            }
        } else {
            bundle.putString(AccountManager.KEY_AUTHTOKEN, data.getString(AccountManager.KEY_AUTHTOKEN));
        }
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, AuthenticAuthenticator.ACCOUNT_TYPE);
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, !TextUtils.isEmpty(bundle.getString(AccountManager.KEY_AUTHTOKEN)));
        return bundle;
    }

    @Override
    public boolean validatePassword(String password) {
        // Does not require password
        return true;
    }
}
