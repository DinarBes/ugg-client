package com.ugg.client.infrastructure.keystore.foundation.external

/**
 * Platform-backed [Keystore] — Keychain on Apple, `EncryptedSharedPreferences` on Android.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TemplateKeystore(configuration: KeystoreConfiguration) : Keystore {
    override val configuration: KeystoreConfiguration

    override fun get(key: String): KeystoreReadResult

    override fun set(
        key: String,
        value: String?,
        accessibility: KeystoreAccessibility,
    ): KeystoreWriteResult
}
