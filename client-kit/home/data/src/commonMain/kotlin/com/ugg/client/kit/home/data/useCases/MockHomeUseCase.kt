package com.ugg.client.kit.home.data.useCases

import com.ugg.client.kit.home.domain.entities.Home
import com.ugg.client.kit.home.domain.useCases.HomeUseCase

/** Bundled content until the home screen is served from Firebase. */
class MockHomeUseCase : HomeUseCase {
    override suspend fun fetch(): Home = Home.mock
}
