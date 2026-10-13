package com.ugg.client.kit.profile.domain.useCases

import com.ugg.client.kit.profile.domain.entities.Profile

interface ProfileUseCase {
    suspend fun fetch(): Profile
}
