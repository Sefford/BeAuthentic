# BeAuthentic

Delightful First-Party Authentication Demo repository

## Abstract

In our aim to develop awesome products, we are to reduce the cognitive friction of the user, and one of the major issues is 0-day retention, including the authentication and onboarding experiences.

## General outline

This app consists on a simple two-activity structure.

### LoginActivity

This activity shows the classic user/password authentication form. It is modified from the simple wizard AndroidStudio provides to accomodate the different strategies we will explain during the talk.
 
If the user is logged, this activity will follow inmediately towards LoggedActivity.

### LoggedActivity

This activity showcases different situations an authenticated activity might run into: Authtoken invalidation, Password change and Logout. 

Additionally this screen will be able to retrieve the AuthToken, which depending on the status of the authentication may respond on a different ways,
outputting its status to a status string within the screen.
 
**Refresh Authtoken:** This option will try to refresh the AuthToken. If the authtoken is invalidated, the Authenticator will try to re-log in the user
 and provide a new one. If the password was deleted, the Authenticator cannot recover and will default to the LoginActivity. 
 
**Invalidate AuthToken:** This will delete the current cached AuthToken.

**Forget Password:** This will invalidate the AuthToken AND delete the current known password.

**Logout:** This will delete the associated account and go back to the LoginActivity.

## Branch "Hanging out with the Locals"

This is the first branch, and besides putting everything in place, the main aim is to show how to use the [AccountManager and a Authenticator](http://developer.android.com/intl/es/training/sync-adapters/creating-authenticator.html) to handle the authentication info
from a simple user/password scheme.

You cannot create new users for the sake of simplicity, and the current login/password is jtkirk/kmaru.
 
Everything about the authentication is delegated to the AccountManager, and the app status is backed by such. Removing the account from the Settings will
produce an inmediate logout of the application, having to login again with the credentials.

Key classes here are `AuthenticAuthenticator`, `AuthenticatorService`, `Sessions` and `account_authenticator.xml`. Also note how the Authenticator is declared
on `AndroidManifest.xml`.

## Branch "Let me Google this login for you"
 
In this second branch, we introduce an additional sign-in system to our app, [Google Sign-in](https://developers.google.com/identity/sign-in/android/start-integrating). This complicates a little the flow, as both Password and Google Sign in
have totally different requirements when concerning the authentication method; Google Sign-in is a passwordless and we will need to carry the login type around the
app.

We added a Strategy pattern for our Authenticator. This a purely design decision, because we want to centralize authentication on the Authenticator itself, however
Google authenticates the accounts on a way that is complicated to perfectly align with the Authenticator philosophy the first time. However, we can do a silent
sign in directly from the Authenticator.

The interesting part here will be how we unify the different ways to log-in. Check `PasswordStrategy` and `GoogleStrategy`.

## Branch "I am Smartlocked"

In the third branch, we add even more complexity by suporting [Smartlock for Passwords](https://developers.google.com/identity/smartlock-passwords/android/). 

This branch does not add much logic to the app itself, and most of it is taken directly from the samples of the feature in Google, which
is basically to retrieve and save the credentials. 

The important points here is to remember that as with Google Sign-in the API heavily uses Intents to resolve abnormal situations with the
credentials (multiple ones, not logged, etc...) and that in order to provide a seamless experience on the sign-in, we need to have it
hooked with our current platform (AccountManaging + Google).

## Branch "Two more things"

In this last branch we will add two main features.

### Single Sign-in
This feature adds a lot of logic, although it will not be adding so much to the Activities themselves. We are introducing [GCM bidirectional 
communication](https://developers.google.com/cloud-messaging/) to the app. 

Before the Sign-in itself, we will be obtaining a token ID from GCM and storing it on a list on a [Firebase](https://www.firebase.com/) instance. We use Firebase to make up for
the lack of backend of this project. Ideally, part of the logic should be replaced by backend logic.

After the credentials are validated we will retrieve the list of available devices and send a push notification for every device but this one.

The remaining devices will receive the credentials and will act accordingly. The key elements are `PushService`, `GCMNotificationService`.

### SyncAdapter

Now that we have supported multidevice and multiplatform, probably we will be interested on keeping all our sessions in sync. For this purpose,
we will be implementing a [SyncAdapter](http://developer.android.com/intl/es/training/sync-adapters/creating-sync-adapter.html) into the app.

One app will update the message on device A, and will post it to Firebase, and notify the other devices that an update has been done. We could do
this via a Firebase listener, but in order to keep it as faithful as possible to a client-server architecture, we will again replace the backend
for a notification into GCM.

This will trigger the other devices to start syncing the message from Firebase, and update the UI via a BroadcastReceiver. `AuthenticSyncAdapter`
and a small modification to `LoggedActivity` and `PushService` is where the interesting pieces of this feature is.

## Certificate 

Data for the Certificate is as follows:

**Keystore Pass:** BeAuthentic
**Keystore Alias:** BeAuthenticSigning
**Key Pass:** BeAuthentic

## License

    Copyright 2016 Saúl Díaz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


