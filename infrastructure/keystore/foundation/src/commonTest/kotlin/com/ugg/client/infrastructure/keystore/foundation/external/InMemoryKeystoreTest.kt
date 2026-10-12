package com.ugg.client.infrastructure.keystore.foundation.external

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryKeystoreTest {

    @Test
    fun missingKeyReadsAsNotFound() {
        assertEquals(KeystoreReadResult.NotFound, InMemoryKeystore().get("absent"))
    }

    @Test
    fun storedValueIsReadBack() {
        val keystore = InMemoryKeystore()

        assertTrue(keystore.set("token", "value").isStored)
        assertEquals(KeystoreReadResult.Found("value"), keystore.get("token"))
    }

    @Test
    fun nullValueRemovesTheEntry() {
        val keystore = InMemoryKeystore()
        keystore.set("token", "value")

        assertTrue(keystore.set("token", null).isStored)
        assertEquals(KeystoreReadResult.NotFound, keystore.get("token"))
        assertNull(keystore.accessibilityOf("token"))
    }

    @Test
    fun notFoundIsDistinctFromTemporarilyUnavailable() {
        // The distinction the whole contract exists for — see AuthorizationStatusTest in api-auth.
        assertTrue(KeystoreReadResult.NotFound != KeystoreReadResult.TemporarilyUnavailable)
    }
}
