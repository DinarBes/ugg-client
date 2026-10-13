package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase

/** Stub profile until accounts and orders come from the backend. */
class MockProfileUseCase : ProfileUseCase {
    override suspend fun fetch(): Profile = Profile.mock
}
