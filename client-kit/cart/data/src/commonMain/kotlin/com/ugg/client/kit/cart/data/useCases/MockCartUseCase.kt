package com.ugg.client.kit.cart.data.useCases

import com.ugg.client.kit.cart.data.mappers.CartCatalog
import com.ugg.client.kit.cart.data.mappers.toCart
import com.ugg.client.kit.cart.data.storage.CartSnapshot
import com.ugg.client.kit.cart.data.storage.CartStorage
import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.cart.domain.entities.CartProduct
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MAX_QUANTITY = 9

/** Device-only cart on top of [CartStorage]; no backend yet. */
class MockCartUseCase(
    private val storage: CartStorage,
) : CartUseCase {

    override fun observeCart(): Flow<Cart> = storage.snapshot.map { it.toCart() }

    override suspend fun addProduct(product: CartProduct) = storage.update {
        it.add(
            CartSnapshot.Line(
                id = product.id,
                title = product.title,
                details = product.details,
                unitPrice = product.unitPrice,
                quantity = 1,
                isExtra = false,
            ),
        )
    }

    override suspend fun addExtra(id: String) {
        val offer = CartCatalog.extras.firstOrNull { it.id == id } ?: return

        storage.update {
            it.add(CartSnapshot.Line(offer.id, offer.title, offer.details, offer.price, quantity = 1, isExtra = true))
        }
    }

    override suspend fun changeQuantity(lineId: String, delta: Int) = storage.update { snapshot ->
        snapshot.copy(
            lines = snapshot.lines.mapNotNull { line ->
                if (line.id != lineId) return@mapNotNull line

                val quantity = (line.quantity + delta).coerceAtMost(MAX_QUANTITY)
                line.copy(quantity = quantity).takeIf { quantity > 0 }
            },
        )
    }

    override suspend fun selectDelivery(id: String) = storage.update { snapshot ->
        if (CartCatalog.deliveries.any { it.id == id }) snapshot.copy(deliveryId = id) else snapshot
    }

    override suspend fun setGiftWrap(enabled: Boolean) = storage.update { it.copy(isGiftWrapped = enabled) }

    private fun CartSnapshot.add(line: CartSnapshot.Line): CartSnapshot {
        val existing = lines.firstOrNull { it.id == line.id }
            ?: return copy(lines = lines + line)

        val quantity = (existing.quantity + 1).coerceAtMost(MAX_QUANTITY)
        return copy(lines = lines.map { if (it.id == line.id) it.copy(quantity = quantity) else it })
    }
}
