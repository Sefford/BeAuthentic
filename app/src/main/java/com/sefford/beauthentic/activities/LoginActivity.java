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

import android.Manifest;
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
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sefford.beauthentic.R;
import com.sefford.beauthentic.auth.AuthenticAuthenticator;
import com.sefford.beauthentic.callbacks.ValueEventListenerAdapter;
import com.sefford.beauthentic.services.LoginGCMNotificationService;
import com.sefford.beauthentic.services.RegistrationIntentService;
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
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_PERMISSION = 0x13;

    @Bind(R.id.et_email)
    AutoCompleteTextView etUsername;
    @Bind(R.id.et_password)
    EditText etPassword;
    @Bind(R.id.pb_progress)
    View pbProgress;
    @Bind(R.id.v_login_form)
    View vLoginForm;
    @Bind(R.id.bt_google_sign)
    SignInButton btGoogleSign;

    AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    Bundle mResultBundle = null;
    GoogleApiAdapter googleApi = GoogleApiAdapter.getInstance();


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
        handleAccountIntent(getIntent());
        googleApi.initialize(this);
        googleApi.connect(this);
        // Set up the login form.
        ButterKnife.bind(this);
        configureView();
        checkPermissions();
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Update 2.0.6 available")
                .setContentText("Now you can mail your favorite artists!")
                .setSmallIcon(R.drawable.common_plus_signin_btn_text_light_pressed)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setLights(getResources().getColor(R.color.colorPrimary), 1000, 1000);

        notificationManager.notify(0x1234, builder.build());
    }

    void configureView() {
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
        btGoogleSign.setSize(SignInButton.SIZE_STANDARD);
    }

    void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_PERMISSION);
        } else {
            refreshGCMToken();
        }
    }

    void handleAccountIntent(Intent intent) {
        mAccountAuthenticatorResponse =
                intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Sessions.isLogged(AccountManager.get(this))) {
            login();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            case GoogleApiAdapter.GOOGLE_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            case GoogleApiAdapter.REGISTER_CREDENTIAL:
                login();
                break;
            case GoogleApiAdapter.RETRIEVE_CREDENTIALS:
                if (resultCode == RESULT_OK) {
                    onCredentialRetrieved((Credential) data.getParcelableExtra(Credential.EXTRA_KEY));
                }
                break;
            case REQUEST_PERMISSION:
                refreshGCMToken();
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleApi.disconnect(googleApi);
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.bt_login)
    public void onLoginClicked() {
        attemptLogin();
    }

    @OnClick(R.id.bt_google_sign)
    public void onGoogleLoginClicked() {
        logAttemptedLogin("google");
        showProgress(true);
        googleApi.performGoogleSignIn(this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    void attemptLogin() {
        logAttemptedLogin("google");
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
        data.putInt(AuthenticAuthenticator.EXTRA_TYPE, AuthenticAuthenticator.Type.PASSWORD.ordinal());
        final Account account = new Account(etUsername.getText().toString(), AuthenticAuthenticator.ACCOUNT_TYPE);
        am.confirmCredentials(account, data, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    final Bundle result = future.getResult();
                    if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                        Sessions.addAccount(am, account, etPassword.getText().toString(), Bundle.EMPTY);
                        am.setAuthToken(account, AuthenticAuthenticator.AUTHTOKEN_TYPE, result.getString(AccountManager.KEY_AUTHTOKEN));
                        am.setUserData(account, AuthenticAuthenticator.EXTRA_TYPE, Integer.toString(AuthenticAuthenticator.Type.PASSWORD.ordinal()));
                        notifyLoginToGCM(AuthenticAuthenticator.Type.PASSWORD.ordinal(), account.name, etPassword.getText().toString(), result.getString(AccountManager.KEY_AUTHTOKEN));
                        logLogin("email");
                        googleApi.saveCredential(new Credential.Builder(account.name)
                                        .setPassword(etPassword.getText().toString()).build(),
                                new SmartlockCredentialCallback());
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
        googleApi.removeCallbacks(this);
        showProgress(false);
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
        if (vLoginForm == null) {
            return;
        }
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
                    if (vLoginForm != null) {
                        vLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                }
            });

            pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            pbProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (vLoginForm != null) {
                        pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                }
            });
        } else {
            if (vLoginForm != null) {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                vLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
    }

    void notifyLoginToGCM(final int type, final String name, final String password, final String authtoken) {
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
                        Intent intent = new Intent(LoginActivity.this, LoginGCMNotificationService.class);
                        intent.putExtra(LoginGCMNotificationService.EXTRA_TYPE, type);
                        intent.putExtra(LoginGCMNotificationService.EXTRA_NAME, name);
                        intent.putExtra(LoginGCMNotificationService.EXTRA_PASSWORD, password);
                        intent.putExtra(LoginGCMNotificationService.EXTRA_AUTHTOKEN, authtoken);
                        intent.putStringArrayListExtra(LoginGCMNotificationService.EXTRA_DEVICES, (ArrayList<String>) snapshot.getValue());
                        startService(intent);
                    }
                }
            });
        }
    }

    void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            createGoogleAccount(acct);
        }
    }

    void createGoogleAccount(final GoogleSignInAccount acct) {
        final Account account = new Account(acct.getDisplayName(), AuthenticAuthenticator.ACCOUNT_TYPE);
        final AccountManager am = AccountManager.get(this);
        final Bundle data = new Bundle();
        data.putInt(AuthenticAuthenticator.EXTRA_TYPE, AuthenticAuthenticator.Type.GOOGLE.ordinal());
        data.putString(AccountManager.KEY_ACCOUNT_NAME, acct.getDisplayName());
        data.putString(AccountManager.KEY_AUTHTOKEN, acct.getIdToken());
        am.confirmCredentials(account, data, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    final Bundle result = future.getResult();
                    if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
                        Sessions.addAccount(am, account, "", Bundle.EMPTY);
                        am.setAuthToken(account, AuthenticAuthenticator.AUTHTOKEN_TYPE, result.getString(AccountManager.KEY_AUTHTOKEN));
                        am.setUserData(account, AuthenticAuthenticator.EXTRA_TYPE, Integer.toString(AuthenticAuthenticator.Type.GOOGLE.ordinal()));
                        notifyLoginToGCM(AuthenticAuthenticator.Type.GOOGLE.ordinal(), account.name, "", result.getString(AccountManager.KEY_AUTHTOKEN));
                        logLogin("google");
                        googleApi.saveCredential(new Credential.Builder(acct.getEmail())
                                .setAccountType(IdentityProviders.GOOGLE)
                                .setName(acct.getDisplayName())
                                .setProfilePictureUri(acct.getPhotoUrl())
                                .build(), new SmartlockCredentialCallback());
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        googleApi.requestCredentials(new ResultCallback<CredentialRequestResult>() {
            @Override
            public void onResult(@NonNull CredentialRequestResult result) {
                if (result.getStatus().isSuccess()) {
                    onCredentialRetrieved(result.getCredential());
                } else if (result.getStatus().getStatusCode() != CommonStatusCodes.SIGN_IN_REQUIRED && result.getStatus().hasResolution()) {
                    try {
                        result.getStatus().startResolutionForResult(LoginActivity.this, GoogleApiAdapter.RETRIEVE_CREDENTIALS);
                    } catch (IntentSender.SendIntentException e) {
                        Snackbar.make(vLoginForm, R.string.error_smartlock_failed, Snackbar.LENGTH_LONG);
                    }
                }
            }
        });
    }

    void onCredentialRetrieved(Credential credential) {
        if (IdentityProviders.GOOGLE.equals(credential.getAccountType())) {
            final GoogleSignInResult result = googleApi.performSilentSignIn().get();
            if (result.isSuccess()) {
                createGoogleAccount(result.getSignInAccount());
            } else if (result.getStatus().hasResolution()) {
                try {
                    result.getStatus().startResolutionForResult(this, GoogleApiAdapter.GOOGLE_SIGN_IN);
                } catch (IntentSender.SendIntentException e) {
                    Snackbar.make(vLoginForm, R.string.error_smartlock_failed, Snackbar.LENGTH_LONG);
                }
            }
        } else {
            etUsername.setText(credential.getId());
            etPassword.setText(credential.getPassword());
            attemptLogin();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    void refreshGCMToken() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    class SmartlockCredentialCallback implements ResultCallback<Status> {

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                login();
            } else if (status.hasResolution() && status.getStatusCode() != CommonStatusCodes.SIGN_IN_REQUIRED) {
                try {
                    status.startResolutionForResult(LoginActivity.this, GoogleApiAdapter.REGISTER_CREDENTIAL);
                } catch (IntentSender.SendIntentException e) {
                    // We couldn't register, but we have to keep on
                    login();
                }
            }
        }
    }

    void logAttemptedLogin(String type) {
        final FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        final Bundle bundle = new Bundle();
        bundle.putString("type", type);
        analytics.logEvent("attempt_login", bundle);
    }

    void logLogin(String type) {
        final FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        final Bundle bundle = new Bundle();
        bundle.putString("type", type);
        analytics.logEvent("login_completed", bundle);
    }
}

