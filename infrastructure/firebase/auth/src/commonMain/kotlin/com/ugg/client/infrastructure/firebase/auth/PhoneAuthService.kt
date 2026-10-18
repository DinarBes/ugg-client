package com.ugg.client.infrastructure.firebase.auth

import kotlinx.coroutines.flow.Flow

/**
 * Phone sign-in. The only place that knows the app signs in with Firebase: swapping it for another
 * provider (Telegram Gateway, an SMS gateway behind a Cloud Function) replaces this service only.
 *
 * Bound once, as a scoped singleton in `client-kit/core/data`.
 */
interface PhoneAuthService {
    /** Current user, re-emitted on every sign-in and sign-out; `null` — a guest. */
    fun observeUser(): Flow<AuthUser?>

    val currentUser: AuthUser?

    /** @throws PhoneAuthException */
    suspend fun sendCode(phone: String): PhoneVerification

    /** @throws PhoneAuthException */
    suspend fun verify(verificationId: String, code: String): AuthUser

    suspend fun signOut()

    /**
     * Deletes the Firebase account of the signed-in user, which also signs them out.
     * @throws PhoneAuthException with `RecentLoginRequired` when the last sign-in is too old.
     */
    suspend fun deleteAccount()
}

/** Platform implementation: the Firebase SDK on Android and the web, the iOS SDK through `IosPhoneAuth` on iOS. */
expect fun createPhoneAuthService(): PhoneAuthService
