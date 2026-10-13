package com.ugg.client.kit.product.domain.useCases

import com.ugg.client.kit.product.domain.entities.Product

interface ProductUseCase {
    suspend fun fetch(): Product
}
