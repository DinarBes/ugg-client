package com.ugg.client.infrastructure.firebase.auth

/** Firebase failures translated into reasons the app can branch on, without Firebase types. */
class PhoneAuthException(
    val reason: Reason,
    cause: Throwable? = null,
) : Exception(reason.name, cause) {

    enum class Reason {
        InvalidPhone,
        InvalidCode,
        /** The verification id is too old; a new code has to be requested. */
        CodeExpired,
        /** Firebase throttled this device or number. */
        TooManyRequests,
        Network,
        /** Deleting the account needs a fresh sign-in: sign out, sign in again, retry. */
        RecentLoginRequired,
        /** Phone sign-in is not available on this platform or in this process (no Activity yet). */
        Unavailable,
        Unknown,
    }
}
