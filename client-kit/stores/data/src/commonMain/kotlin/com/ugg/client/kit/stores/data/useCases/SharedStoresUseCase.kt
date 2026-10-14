@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.stores.data.useCases

import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.stores.data.mappers.toDomain
import com.ugg.client.kit.stores.domain.entities.Store
import com.ugg.client.kit.stores.domain.useCases.StoresUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The stores from Firestore, in their `order`. «Открыто» is worked out for [now] on every fetch. */
class SharedStoresUseCase(
    private val catalog: CatalogService,
    private val now: () -> Instant = { Clock.System.now() },
) : StoresUseCase {

    override suspend fun fetch(): List<Store> = translatingContentErrors {
        val moment = now()
        catalog.stores().map { it.toDomain(moment) }
    }
}
