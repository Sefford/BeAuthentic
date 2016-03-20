package com.sefford.beauthentic.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.sefford.beauthentic.R;
import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.utils.Sessions;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sefford on 20/03/16.
 */
public class LoggedActivity extends AppCompatActivity {

    @Bind(R.id.tv_status)
    TextView tvStatus;

    AccountManager am;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_logged);
        am = AccountManager.get(getApplicationContext());
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Sessions.isLogged(am)) {
            logout();
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
            am.getAuthToken(Sessions.getAccount(am), AuthenticAuthenticator.AUTHTOKEN_TYPE, Bundle.EMPTY, true, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        final Bundle result = future.getResult();
                        if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                            tvStatus.setText(R.string.status_logged);
                        } else {
                            // We would be returning a intent here to open Login Activity, so we do not care, actually
                            logout();
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

    @OnClick(R.id.bt_logout)
    public void onLoggingOut() {
        am.removeAccount(Sessions.getAccount(am), new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                logout();
            }
        }, null);
    }
}
