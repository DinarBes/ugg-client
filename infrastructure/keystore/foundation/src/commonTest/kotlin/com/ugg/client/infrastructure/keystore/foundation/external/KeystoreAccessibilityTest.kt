package com.ugg.client.infrastructure.keystore.foundation.external

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Contract coverage for per-entry accessibility, asserted against [InMemoryKeystore] as the
 * reference implementation. A store pinned to a single level would fail these.
 */
class KeystoreAccessibilityTest {

    @Test
    fun defaultLevelKeepsTokensReadableAfterFirstUnlock() {
        assertEquals(
            KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly,
            KeystoreConfiguration().defaultAccessibility,
        )
    }

    @Test
    fun writeWithoutAnExplicitLevelUsesTheConfiguredDefault() {
        val configuration = KeystoreConfiguration().apply {
            defaultAccessibility = KeystoreAccessibility.WhenUnlocked
        }
        val keystore = InMemoryKeystore(configuration)

        keystore.set("token", "value")

        assertEquals(KeystoreAccessibility.WhenUnlocked, keystore.accessibilityOf("token"))
    }

    @Test
    fun entriesCanCarryDifferentLevels() {
        val keystore = InMemoryKeystore()

        keystore.set("refresh-token", "r", KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly)
        keystore.set("pin-hash", "p", KeystoreAccessibility.WhenPasscodeSetThisDeviceOnly)

        assertEquals(
            KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly,
            keystore.accessibilityOf("refresh-token"),
        )
        assertEquals(
            KeystoreAccessibility.WhenPasscodeSetThisDeviceOnly,
            keystore.accessibilityOf("pin-hash"),
        )
    }

    @Test
    fun rewritingAnEntryWithANewLevelActuallyChangesIt() {
        // Silently keeping the old protection class is exactly the class of bug this module fixes.
        val keystore = InMemoryKeystore()

        keystore.set("token", "value", KeystoreAccessibility.WhenUnlocked)
        keystore.set("token", "value", KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly)

        assertEquals(
            KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly,
            keystore.accessibilityOf("token"),
        )
    }

    @Test
    fun changingOneEntryLeavesOthersAlone() {
        val keystore = InMemoryKeystore()

        keystore.set("a", "1", KeystoreAccessibility.WhenUnlocked)
        keystore.set("b", "2", KeystoreAccessibility.WhenUnlocked)
        keystore.set("a", "1", KeystoreAccessibility.AfterFirstUnlock)

        assertEquals(KeystoreAccessibility.AfterFirstUnlock, keystore.accessibilityOf("a"))
        assertEquals(KeystoreAccessibility.WhenUnlocked, keystore.accessibilityOf("b"))
    }
}
