package com.ugg.client.kit.care.domain.useCases

import com.ugg.client.kit.care.domain.entities.Care

interface CareUseCase {
    suspend fun fetch(): Care
}
