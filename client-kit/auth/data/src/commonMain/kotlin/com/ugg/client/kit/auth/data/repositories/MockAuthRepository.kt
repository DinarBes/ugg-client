package com.ugg.client.kit.auth.data.repositories

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.auth.TemplateTokens
import com.ugg.client.infrastructure.api.auth.context.AuthEvent
import com.ugg.client.kit.auth.domain.entities.PhoneChallenge
import com.ugg.client.kit.auth.domain.exceptions.InvalidCodeException
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase

class MockAuthRepository(
    private val auth: TemplateAuth
) : AuthUseCase {
    private val context by auth.context

    override suspend fun initiate(phone: String): PhoneChallenge = PhoneChallenge.CodeSent("stub-session-id")

    override suspend fun login(otp: String, sessionId: String) {
        // MOCK accepts only 123456.
        if (otp != "123456") throw InvalidCodeException()

        // Exercise the same write seam the real flow uses. In MOCK the keystore is an
        // InMemoryKeystore, so these placeholder tokens live only for the session.
        auth.store(TemplateTokens(accessToken = "mock-access-token", refreshToken = "mock-refresh-token"))
        context.send(AuthEvent.Login)
    }
}
