package com.ugg.client.kit.stores.domain.useCases

import com.ugg.client.kit.stores.domain.entities.Store

interface StoresUseCase {
    suspend fun fetch(): List<Store>
}
