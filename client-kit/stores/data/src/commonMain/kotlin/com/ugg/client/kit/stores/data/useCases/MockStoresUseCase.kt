package com.ugg.client.kit.stores.data.useCases

import com.ugg.client.kit.stores.domain.entities.Store
import com.ugg.client.kit.stores.domain.useCases.StoresUseCase

/** Bundled stores until they are served from Firebase. */
class MockStoresUseCase : StoresUseCase {
    override suspend fun fetch(): List<Store> = Store.mock
}
