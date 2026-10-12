package com.ugg.client.infrastructure.keystore.foundation.external

/**
 * Outcome of [Keystore.get].
 *
 * [NotFound] and [TemporarilyUnavailable] are deliberately separate: collapsing both into `null`
 * is what makes a launch screen sign out a perfectly valid user after a background wake-up.
 */
sealed interface KeystoreReadResult {
    data class Found(val value: String) : KeystoreReadResult

    /** No entry under this key. On Apple: `errSecItemNotFound`. */
    data object NotFound : KeystoreReadResult

    /**
     * The entry may exist but cannot be read right now — the device has not been unlocked since
     * boot. On Apple: `errSecInteractionNotAllowed`. Never a reason to conclude "not authorized".
     */
    data object TemporarilyUnavailable : KeystoreReadResult

    data class Failure(val reason: KeystoreFailure) : KeystoreReadResult
}

/** The stored value, or `null` when it is absent, unreadable, or errored. */
fun KeystoreReadResult.valueOrNull(): String? =
    (this as? KeystoreReadResult.Found)?.value
