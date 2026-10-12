package com.ugg.client.infrastructure.keystore.foundation.external

/**
 * When a stored entry is readable, expressed in template terms rather than platform constants.
 *
 * The mapping onto `kSecAttrAccessible*` lives in `appleMain` — common code never depends on
 * Security framework constants. Android has no per-entry equivalent; see [Keystore.set].
 */
enum class KeystoreAccessibility {
    /** Only while the device is unlocked. Strictest useful level for interactive-only secrets. */
    WhenUnlocked,

    /** [WhenUnlocked], and never restored onto another device. */
    WhenUnlockedThisDeviceOnly,

    /** From the first unlock after boot onwards, including while the screen is locked. */
    AfterFirstUnlock,

    /**
     * [AfterFirstUnlock], and never restored onto another device.
     *
     * Default for auth tokens: the app may be launched into the background while the screen is
     * locked, and a token that cannot be read there would look like a signed-out user.
     */
    AfterFirstUnlockThisDeviceOnly,

    /** Only while unlocked, and only if a passcode is set. Never leaves the device. */
    WhenPasscodeSetThisDeviceOnly,
}
