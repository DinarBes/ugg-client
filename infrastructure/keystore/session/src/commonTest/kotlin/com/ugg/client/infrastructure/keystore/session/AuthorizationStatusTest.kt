package com.ugg.client.infrastructure.keystore.session

import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreConfiguration
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreFailure
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreWriteResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Regression coverage for the background-launch crash: on iOS the process may start while the
 * screen is locked, and the Keychain answers `errSecInteractionNotAllowed`. "Cannot read the token
 * right now" must never be collapsed into "the user is not authorized".
 */
class AuthorizationStatusTest {

    private class FixedKeystore(private val result: KeystoreReadResult) : Keystore {
        override val configuration = KeystoreConfiguration()

        override fun get(key: String): KeystoreReadResult = result

        override fun set(
            key: String,
            value: String?,
            accessibility: KeystoreAccessibility,
        ): KeystoreWriteResult = KeystoreWriteResult.Stored
    }

    private fun statusFor(result: KeystoreReadResult) =
        AccessService(FixedKeystore(result)).authorizationStatus()

    @Test
    fun temporarilyUnavailableIsNotTreatedAsUnauthorized() {
        val status = statusFor(KeystoreReadResult.TemporarilyUnavailable)

        assertNotEquals(AuthorizationStatus.NotAuthorized, status)
        assertEquals(AuthorizationStatus.Undetermined, status)
    }

    @Test
    fun missingTokenMeansNotAuthorized() {
        assertEquals(AuthorizationStatus.NotAuthorized, statusFor(KeystoreReadResult.NotFound))
    }

    @Test
    fun presentTokenMeansAuthorized() {
        assertEquals(AuthorizationStatus.Authorized, statusFor(KeystoreReadResult.Found("refresh")))
    }

    @Test
    fun failureIsNotTreatedAsUnauthorized() {
        val status = statusFor(KeystoreReadResult.Failure(KeystoreFailure.PlatformStatus(-25308)))

        assertNotEquals(AuthorizationStatus.NotAuthorized, status)
        assertEquals(AuthorizationStatus.Undetermined, status)
    }
}
