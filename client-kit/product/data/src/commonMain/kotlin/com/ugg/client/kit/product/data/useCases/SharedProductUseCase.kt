package com.ugg.client.kit.product.data.useCases

import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.product.data.mappers.toDomain
import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.useCases.ProductUseCase

/** The model from Firestore, `products/classic`. */
class SharedProductUseCase(
    private val catalog: CatalogService,
) : ProductUseCase {

    override suspend fun fetch(): Product = translatingContentErrors { catalog.product().toDomain() }
}
