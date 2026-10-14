package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.kit.profile.domain.entities.FootSize
import com.ugg.client.kit.profile.domain.entities.SizeOptions
import com.ugg.client.kit.profile.domain.useCases.SizeUseCase

/** Keeps the size in memory for the session. */
class MockSizeUseCase : SizeUseCase {
    private var size = FootSize(size = "38", heightId = "standard")

    override suspend fun options() = SizeOptions(
        sizes = (35..45).map { it.toString() },
        heights = listOf(
            SizeOptions.HeightOption("standard", "Стандарт", "22 см"),
            SizeOptions.HeightOption("tall", "Высокие", "30 см"),
        ),
    )

    override suspend fun current() = size

    override suspend fun save(size: FootSize) {
        this.size = size
    }
}
