package com.sefford.beauthentic.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.sefford.beauthentic.R;
import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.utils.Sessions;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    @Bind(R.id.et_email)
    AutoCompleteTextView etUsername;
    @Bind(R.id.et_password)
    EditText etPassword;
    @Bind(R.id.pb_progress)
    View pbProgress;
    @Bind(R.id.v_login_form)
    View vLoginForm;
    @Bind(R.id.bt_login)
    View btLogin;

    AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    Bundle mResultBundle = null;

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_login);
        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
        // Set up the login form.
        ButterKnife.bind(this);
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        btLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Sessions.isLogged(AccountManager.get(this))) {
            login();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    void attemptLogin() {
        // Reset errors.
        etUsername.setError(null);
        etPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            etUsername.setError(getString(R.string.error_field_required));
            focusView = etUsername;
            cancel = true;
        } else if (!isUsernameValid(email)) {
            etUsername.setError(getString(R.string.error_invalid_email));
            focusView = etUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            performLogin();
        }
    }

    void performLogin() {
        final AccountManager am = AccountManager.get(this);
        final Bundle data = new Bundle();
        data.putString(AuthenticAuthenticator.EXTRA_PASSWORD, etPassword.getText().toString());
        final Account account = new Account(etUsername.getText().toString(), AuthenticAuthenticator.ACCOUNT_TYPE);
        am.confirmCredentials(account, data, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    showProgress(false);
                    final Bundle result = future.getResult();
                    if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                        am.addAccountExplicitly(account, etPassword.getText().toString(), Bundle.EMPTY);
                        am.setAuthToken(account, AuthenticAuthenticator.AUTHTOKEN_TYPE, result.getString(AccountManager.KEY_AUTHTOKEN));
                        login();
                    } else {
                        Snackbar.make(vLoginForm, R.string.error_invalid_credentials, Snackbar.LENGTH_LONG).show();
                    }
                } catch (OperationCanceledException e) {
                    Snackbar.make(vLoginForm, R.string.error_operation_cancelled, Snackbar.LENGTH_LONG).show();
                } catch (IOException e) {
                    Snackbar.make(vLoginForm, R.string.error_not_connected_to_internet, Snackbar.LENGTH_LONG).show();
                } catch (AuthenticatorException e) {
                    Snackbar.make(vLoginForm, R.string.error_invalid_credentials, Snackbar.LENGTH_LONG).show();
                }
            }
        }, null);
    }

    void login() {
        finish();
        Intent intent = new Intent(getApplicationContext(), LoggedActivity.class);
        startActivity(intent);
    }

    boolean isUsernameValid(String email) {
        return !email.isEmpty();
    }

    boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            vLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            vLoginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    vLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            pbProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            vLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}

