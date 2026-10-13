package com.ugg.client.kit.product.data.useCases

import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.useCases.ProductUseCase

/** Bundled content until the product is served from Firebase. */
class MockProductUseCase : ProductUseCase {
    override suspend fun fetch(): Product = Product.mock
}
