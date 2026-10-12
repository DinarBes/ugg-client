package com.ugg.client.infrastructure.keystore.foundation.external

/**
 * Secure key-value storage for small secrets.
 *
 * Neither operation throws: every outcome is a value. This is not a style preference — on
 * Kotlin/Native an undeclared exception crossing the Objective-C boundary is an unconditional
 * `abort()`, which no Swift `do/catch` can intercept.
 */
interface Keystore {
    val configuration: KeystoreConfiguration

    fun get(key: String): KeystoreReadResult

    /**
     * Stores [value] under [key], or removes the entry when [value] is `null`.
     *
     * [accessibility] is chosen per entry so that a token needed during a locked-screen background
     * launch does not force the whole store down to the weakest protection class. It defaults to
     * [KeystoreConfiguration.defaultAccessibility].
     *
     * On Android there is no per-entry equivalent, so a successful write reports
     * [KeystoreWriteResult.StoredWithoutAccessibility] rather than claiming the level was applied.
     */
    fun set(
        key: String,
        value: String?,
        accessibility: KeystoreAccessibility = configuration.defaultAccessibility,
    ): KeystoreWriteResult
}
