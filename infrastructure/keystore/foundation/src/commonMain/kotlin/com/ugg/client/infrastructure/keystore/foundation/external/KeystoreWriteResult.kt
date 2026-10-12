package com.ugg.client.infrastructure.keystore.foundation.external

/** Outcome of [Keystore.set]. */
sealed interface KeystoreWriteResult {
    /** Written, with the requested [KeystoreAccessibility] applied. */
    data object Stored : KeystoreWriteResult

    /**
     * Written, but the platform has no notion of per-entry accessibility, so [requested] was not
     * applied. Reported by the Android implementation rather than silently pretending otherwise.
     */
    data class StoredWithoutAccessibility(
        val requested: KeystoreAccessibility,
    ) : KeystoreWriteResult

    /** The device has not been unlocked since boot, so the entry could not be written. */
    data object TemporarilyUnavailable : KeystoreWriteResult

    data class Failure(val reason: KeystoreFailure) : KeystoreWriteResult
}

/** Whether the value reached storage, regardless of whether the accessibility level was honoured. */
val KeystoreWriteResult.isStored: Boolean
    get() = this is KeystoreWriteResult.Stored ||
        this is KeystoreWriteResult.StoredWithoutAccessibility
