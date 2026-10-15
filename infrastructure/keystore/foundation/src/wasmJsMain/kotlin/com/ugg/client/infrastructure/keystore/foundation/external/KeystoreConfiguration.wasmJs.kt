package com.ugg.client.infrastructure.keystore.foundation.external

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class KeystoreConfiguration {
    actual var defaultAccessibility: KeystoreAccessibility =
        KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly

    /** Prefix of every key in `localStorage`, so entries do not collide with other data of the site. */
    var prefix: String = "ugg."
}
