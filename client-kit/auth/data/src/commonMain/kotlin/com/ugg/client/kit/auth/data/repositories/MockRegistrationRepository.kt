package com.ugg.client.kit.auth.data.repositories

import com.ugg.client.kit.auth.domain.useCases.RegistrationUseCase

/** Nothing to save without a backend; the step just closes. */
class MockRegistrationRepository : RegistrationUseCase {
    override suspend fun saveName(name: String) = Unit
}
