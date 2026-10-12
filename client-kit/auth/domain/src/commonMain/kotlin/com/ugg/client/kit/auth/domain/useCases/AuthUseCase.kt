package com.ugg.client.kit.auth.domain.useCases

interface AuthUseCase {
    suspend fun initiate(phone: String): String
    suspend fun login(otp: String, sessionId: String)
}
