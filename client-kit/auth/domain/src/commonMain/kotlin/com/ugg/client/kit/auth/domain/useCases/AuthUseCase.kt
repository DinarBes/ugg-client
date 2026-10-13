package com.ugg.client.kit.auth.domain.useCases

import com.ugg.client.kit.auth.domain.entities.PhoneChallenge

interface AuthUseCase {
    /**
     * Sends a code to [phone] («+79001234567»).
     *
     * @throws com.ugg.client.kit.auth.domain.exceptions.InvalidPhoneException
     * @throws com.ugg.client.kit.auth.domain.exceptions.TooManyAttemptsException
     */
    suspend fun initiate(phone: String): PhoneChallenge

    /**
     * @throws com.ugg.client.kit.auth.domain.exceptions.InvalidCodeException
     * @throws com.ugg.client.kit.auth.domain.exceptions.CodeExpiredException
     */
    suspend fun login(otp: String, sessionId: String)
}
