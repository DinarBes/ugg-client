package com.ugg.client.kit.profile.domain.useCases

import com.ugg.client.kit.profile.domain.entities.FootSize
import com.ugg.client.kit.profile.domain.entities.SizeOptions

interface SizeUseCase {
    suspend fun options(): SizeOptions
    suspend fun current(): FootSize
    suspend fun save(size: FootSize)
}
