package com.ugg.client.kit.cart.domain.useCases

import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.cart.domain.entities.CartProduct
import kotlinx.coroutines.flow.Flow

/**
 * One cart for the whole app. Every screen that shows it collects [observeCart], so a change made
 * on one screen (the product page adding a pair) reaches the others without them talking.
 */
interface CartUseCase {
    fun observeCart(): Flow<Cart>

    suspend fun addProduct(product: CartProduct)

    suspend fun addExtra(id: String)

    /** Quantity drops to zero → the line is removed. */
    suspend fun changeQuantity(lineId: String, delta: Int)

    suspend fun selectDelivery(id: String)

    suspend fun setGiftWrap(enabled: Boolean)
}
