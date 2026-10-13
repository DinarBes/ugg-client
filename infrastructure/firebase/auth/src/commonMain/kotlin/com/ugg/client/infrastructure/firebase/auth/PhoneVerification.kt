package com.ugg.client.infrastructure.firebase.auth

/** Outcome of asking for a code. */
sealed interface PhoneVerification {
    /** An SMS is on its way; pass [verificationId] back with the code. */
    data class CodeSent(val verificationId: String) : PhoneVerification

    /**
     * Android verified the number by itself (instant verification or an auto-read SMS) and the
     * user is already signed in — there is no code to ask for.
     */
    data class SignedIn(val user: AuthUser) : PhoneVerification
}
