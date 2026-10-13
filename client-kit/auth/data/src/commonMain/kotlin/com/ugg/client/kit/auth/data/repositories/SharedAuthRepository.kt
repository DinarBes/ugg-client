package com.ugg.client.kit.auth.data.repositories

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneVerification
import com.ugg.client.kit.auth.data.mappers.translatingAuthErrors
import com.ugg.client.kit.auth.domain.entities.PhoneChallenge
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase

/**
 * Firebase phone sign-in. Nothing is announced after a successful login: the splash watches the
 * signed-in user itself and takes the app forward.
 */
class SharedAuthRepository(
    private val phoneAuth: PhoneAuthService,
) : AuthUseCase {

    override suspend fun initiate(phone: String): PhoneChallenge = translatingAuthErrors {
        when (val verification = phoneAuth.sendCode(phone)) {
            is PhoneVerification.CodeSent -> PhoneChallenge.CodeSent(verification.verificationId)
            is PhoneVerification.SignedIn -> PhoneChallenge.SignedIn
        }
    }

    override suspend fun login(otp: String, sessionId: String) = translatingAuthErrors {
        phoneAuth.verify(verificationId = sessionId, code = otp)
        Unit
    }
}
