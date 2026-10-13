package com.ugg.client.kit.auth.domain.entities

/** What happens after the phone number is sent. */
sealed interface PhoneChallenge {
    /** An SMS is on its way; the code goes back together with [sessionId]. */
    data class CodeSent(val sessionId: String) : PhoneChallenge

    /** The number was verified without a code (Android can do this); the user is signed in. */
    data object SignedIn : PhoneChallenge
}
