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
package com.sefford.beauthentic.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;

import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.providers.MessageProvider;

/**
 * Sessions utils adapter.
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class Sessions {

    private static final String GOOGLE_ACCOUNT = "com.google";

    private Sessions() {
    }

    public static final boolean isLogged(AccountManager accountManager) {
        return accountManager.getAccountsByType(AuthenticAuthenticator.ACCOUNT_TYPE).length > 0;
    }

    public static final Account getAccount(AccountManager accountManager) {
        return isLogged(accountManager) ? accountManager.getAccountsByType(AuthenticAuthenticator.ACCOUNT_TYPE)[0] : null;
    }

    public static final Account getPrimaryPhoneAccount(AccountManager accountManager) {
        return accountManager.getAccountsByType(GOOGLE_ACCOUNT).length > 0 ? accountManager.getAccountsByType(GOOGLE_ACCOUNT)[0] : null;
    }

    public static final void enableSyncing(Account newAccount, String authority) {
        ContentResolver.setIsSyncable(newAccount, authority, 1);
        ContentResolver.setSyncAutomatically(newAccount, authority, true);
        ContentResolver.addPeriodicSync(newAccount, authority, Bundle.EMPTY, 60);
    }

    public static void addAccount(AccountManager am, Account account, String password, Bundle empty) {
        am.addAccountExplicitly(account, password, empty);
        enableSyncing(account, MessageProvider.AUTHORITY);
    }
}
