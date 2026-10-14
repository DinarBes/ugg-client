package com.ugg.client.kit.auth.domain.useCases

/** The step right after the first sign-in: how the workshop should address the customer. */
interface RegistrationUseCase {
    /** Saves the name to the signed-in user's profile. */
    suspend fun saveName(name: String)
}
