@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.home.data.useCases

import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.home.data.mappers.homeFrom
import com.ugg.client.kit.home.domain.entities.Home
import com.ugg.client.kit.home.domain.useCases.HomeUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The home screen from Firestore: `content/home`, `products/classic` and `stores`, read in
 * parallel. Store badges («до 20:00») are worked out for [now], so they are fresh on every fetch.
 */
class SharedHomeUseCase(
    private val catalog: CatalogService,
    private val now: () -> Instant = { Clock.System.now() },
) : HomeUseCase {

    override suspend fun fetch(): Home = translatingContentErrors {
        coroutineScope {
            val content = async { catalog.home() }
            val product = async { catalog.product() }
            val stores = async { catalog.stores() }

            homeFrom(content.await(), product.await(), stores.await(), now())
        }
    }
}
