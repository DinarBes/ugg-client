package com.ugg.client.infrastructure.keystore.session

/**
 * What the stored credentials say about the session.
 *
 * A boolean cannot express this. On iOS the app may be launched into the background before the
 * device has been unlocked since boot, and the Keychain refuses to hand over the token. Folding
 * that into `false` signs out a perfectly valid user.
 */
sealed interface AuthorizationStatus {
    data object Authorized : AuthorizationStatus

    data object NotAuthorized : AuthorizationStatus

    /** Credentials could not be read. Not a statement about whether the user is signed in. */
    data object Undetermined : AuthorizationStatus
}
