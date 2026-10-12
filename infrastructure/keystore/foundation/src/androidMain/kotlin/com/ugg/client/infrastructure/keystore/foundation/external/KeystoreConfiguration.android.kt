package com.ugg.client.infrastructure.keystore.foundation.external

import android.content.Context

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class KeystoreConfiguration {
    /**
     * Recorded for parity with the Apple implementation, but Android has no per-entry protection
     * class — see [TemplateKeystore.set].
     */
    actual var defaultAccessibility: KeystoreAccessibility =
        KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly

    var context: Context? = null

    var filename: String = "template_client_secure_storage"

    /**
     * Whether to fall back to unencrypted `SharedPreferences` when `EncryptedSharedPreferences`
     * cannot be created.
     *
     * `false` by default, and that default is the point: the implementation this replaces fell back
     * silently and wrote auth tokens in plaintext, reporting nothing beyond a `Log.e`. With the
     * fallback off, every operation returns [KeystoreFailure.EncryptionUnavailable] instead.
     */
    var allowUnencryptedFallback: Boolean = false
}
