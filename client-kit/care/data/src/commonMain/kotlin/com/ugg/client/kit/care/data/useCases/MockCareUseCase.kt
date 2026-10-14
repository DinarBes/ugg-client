package com.ugg.client.kit.care.data.useCases

import com.ugg.client.kit.care.domain.entities.Care
import com.ugg.client.kit.care.domain.useCases.CareUseCase

class MockCareUseCase : CareUseCase {
    override suspend fun fetch(): Care = Care.mock
}
