package com.ugg.client.infrastructure.firebase.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// TODO: back this with the Firebase iOS SDK (through a Swift implementation of PhoneAuthService
// handed to TemplateApp) once the iOS app is in the repository.
actual fun createPhoneAuthService(): PhoneAuthService = UnavailablePhoneAuthService()

/** Everyone is a guest on iOS for now; asking for a code reports that sign-in is unavailable. */
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
