package com.ugg.client.infrastructure.keystore.foundation.internal

import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFStringRef
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly
import platform.Security.kSecAttrAccessibleWhenUnlocked
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly

/** The `kSecAttrAccessible*` constant behind a template accessibility level. */
@OptIn(ExperimentalForeignApi::class)
internal val KeystoreAccessibility.cfValue: CFStringRef?
    get() = when (this) {
        KeystoreAccessibility.WhenUnlocked -> kSecAttrAccessibleWhenUnlocked
        KeystoreAccessibility.WhenUnlockedThisDeviceOnly -> kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        KeystoreAccessibility.AfterFirstUnlock -> kSecAttrAccessibleAfterFirstUnlock
        KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly -> kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        KeystoreAccessibility.WhenPasscodeSetThisDeviceOnly -> kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly
    }
