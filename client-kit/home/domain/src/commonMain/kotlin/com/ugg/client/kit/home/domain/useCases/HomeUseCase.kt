package com.ugg.client.kit.home.domain.useCases

import com.ugg.client.kit.home.domain.entities.Home

interface HomeUseCase {
    suspend fun fetch(): Home
}
