package com.ugg.client.infrastructure.keystore.foundation.external

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class KeystoreConfiguration {
    actual var defaultAccessibility: KeystoreAccessibility =
        KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly

    /** Keychain access group, for sharing entries with app extensions. */
    var accessGroup: String? = null

    /** `kSecAttrService`, scoping entries within the app's keychain. */
    var service: String? = null
}
