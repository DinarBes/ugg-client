package com.ugg.client.infrastructure.firebase.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Until the iOS app sets [IosPhoneAuth.bridge] (or in a host without Firebase): everyone is a
 * guest, and asking for a code reports that sign-in is unavailable.
 */
internal class UnavailablePhoneAuthService : PhoneAuthService {
    override fun observeUser(): Flow<AuthUser?> = flowOf(null)

    override val currentUser: AuthUser? = null

    override suspend fun sendCode(phone: String): PhoneVerification =
        throw PhoneAuthException(PhoneAuthException.Reason.Unavailable)

    override suspend fun verify(verificationId: String, code: String): AuthUser =
        throw PhoneAuthException(PhoneAuthException.Reason.Unavailable)

    override suspend fun signOut() = Unit

    override suspend fun deleteAccount() = Unit
}
