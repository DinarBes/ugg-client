package com.ugg.client.infrastructure.keystore.session

import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreWriteResult
import com.ugg.client.infrastructure.keystore.foundation.external.isStored
import com.ugg.client.infrastructure.keystore.foundation.external.valueOrNull

/**
 * The app's session token store, backed by the keystore.
 *
 * This is the seam that keeps `infrastructure:api` free of any keystore dependency: the HTTP layer
 * receives tokens through a builder block (see `TemplateAuth`'s `bearerAuthConfig`), and this
 * service — living beside the keystore, not inside the API — is what that block reads from.
 *
 * Reads return [KeystoreReadResult] rather than `String?` so callers deciding whether the user is
 * signed in can tell "no token stored" from "cannot read the token right now" — see
 * [authorizationStatus].
 */
class AccessService(val keystore: Keystore) {
    companion object {
        private const val ACCESS_TOKEN_KEY = "access-token"
        private const val REFRESH_TOKEN_KEY = "refresh-token"

        /**
         * Tokens must be readable during a background launch on a locked device, so they carry
         * their own accessibility rather than inheriting a possibly stricter store-wide default.
         */
        private val TOKEN_ACCESSIBILITY = KeystoreAccessibility.AfterFirstUnlockThisDeviceOnly
    }

    fun accessToken(): KeystoreReadResult = keystore.get(ACCESS_TOKEN_KEY)

    fun refreshToken(): KeystoreReadResult = keystore.get(REFRESH_TOKEN_KEY)

    /** The token, or `null` when absent, unreadable, or errored. For request signing only. */
    fun accessTokenOrNull(): String? = accessToken().valueOrNull()

    fun refreshTokenOrNull(): String? = refreshToken().valueOrNull()

    fun setAccessToken(value: String?): KeystoreWriteResult =
        keystore.set(ACCESS_TOKEN_KEY, value, TOKEN_ACCESSIBILITY)

    fun setRefreshToken(value: String?): KeystoreWriteResult =
        keystore.set(REFRESH_TOKEN_KEY, value, TOKEN_ACCESSIBILITY)

    /**
     * Writes both tokens, reporting the first outcome that did not reach storage.
     *
     * Both writes are always attempted — short-circuiting would let a failed access-token write
     * leave a stale refresh token behind on [clear].
     */
    fun store(accessToken: String?, refreshToken: String?): KeystoreWriteResult {
        val access = setAccessToken(accessToken)
        val refresh = setRefreshToken(refreshToken)

        return if (access.isStored) refresh else access
    }

    fun clear(): KeystoreWriteResult = store(accessToken = null, refreshToken = null)

    /**
     * Derives the session state from the refresh token.
     *
     * `TemporarilyUnavailable` and read failures map to [AuthorizationStatus.Undetermined], never
     * to [AuthorizationStatus.NotAuthorized]: "cannot read" is not "signed out".
     */
    fun authorizationStatus(): AuthorizationStatus = when (refreshToken()) {
        is KeystoreReadResult.Found -> AuthorizationStatus.Authorized
        KeystoreReadResult.NotFound -> AuthorizationStatus.NotAuthorized
        KeystoreReadResult.TemporarilyUnavailable -> AuthorizationStatus.Undetermined
        is KeystoreReadResult.Failure -> AuthorizationStatus.Undetermined
    }
}
