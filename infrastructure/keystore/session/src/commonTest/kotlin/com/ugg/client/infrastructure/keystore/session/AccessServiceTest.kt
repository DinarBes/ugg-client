package com.ugg.client.infrastructure.keystore.session

import com.ugg.client.infrastructure.keystore.foundation.external.InMemoryKeystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.isStored
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessServiceTest {

    @Test
    fun tokensAreReadBackThroughTheBackingKeystore() {
        val access = AccessService(InMemoryKeystore())

        assertTrue(access.setAccessToken("test-token").isStored)

        assertEquals(KeystoreReadResult.Found("test-token"), access.accessToken())
        assertEquals("test-token", access.accessTokenOrNull())
    }

    @Test
    fun tokensAreStoredSoTheySurviveALockedScreen() {
        val keystore = InMemoryKeystore()
        val access = AccessService(keystore)

        access.store(accessToken = "a", refreshToken = "r")

        assertEquals(
            KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly,
            keystore.accessibilityOf("refresh-token"),
        )
    }

    @Test
    fun clearRemovesBothTokens() {
        val access = AccessService(InMemoryKeystore())

        access.store(accessToken = "a", refreshToken = "r")
        assertTrue(access.clear().isStored)

        assertEquals(KeystoreReadResult.NotFound, access.accessToken())
        assertEquals(KeystoreReadResult.NotFound, access.refreshToken())
    }
}
