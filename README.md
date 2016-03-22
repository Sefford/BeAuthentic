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

This is the first branch, and besides putting everything in place, the main aim is to show how to use the AccountManager to handle the authentication info
from a simple user/password scheme.

You cannot create new users for the sake of simplicity, and the current login/password is jtkirk/kmaru.
 
Everything about the authentication is delegated to the AccountManager, and the app status is backed by such. Removing the account from the Settings will
produce an inmediate logout of the application, having to login again with the credentials.

Key classes here are `AuthenticAuthenticator`, `AuthenticatorService`, `Sessions` and `account_authenticator.xml`. Also note how the Authenticator is declared
on `AndroidManifest.xml`.

## Certificate 

Data for the Certificate is as follows:

**Keystore Pass:** BeAuthentic
**Keystore Alias:** BeAuthenticSigning
**Key Pass:** BeAuthentic
