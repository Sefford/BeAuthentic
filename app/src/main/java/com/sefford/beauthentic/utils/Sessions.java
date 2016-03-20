package com.sefford.beauthentic.utils;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.sefford.beauthentic.auth.AuthenticAuthenticator;

/**
 * Created by sefford on 20/03/16.
 */
public class Sessions {

    public static final boolean isLogged(AccountManager accountManager) {
        return accountManager.getAccountsByType(AuthenticAuthenticator.ACCOUNT_TYPE).length > 0;
    }

    public static final Account getAccount(AccountManager accountManager) {
        return isLogged(accountManager) ? accountManager.getAccountsByType(AuthenticAuthenticator.ACCOUNT_TYPE)[0] : null;
    }
}
