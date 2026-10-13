package com.ugg.client.kit.cart.data.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * The cart's single source of truth while it lives on the device only. Must be bound as a
 * singleton: every screen has to read and write the same instance.
 *
 * In memory for now — the cart is lost when the app is killed. Persist it (or move it to the
 * backend) together with checkout.
 */
class CartStorage {
    val snapshot: StateFlow<CartSnapshot>
        field = MutableStateFlow(CartSnapshot())

    fun update(transform: (CartSnapshot) -> CartSnapshot) = snapshot.update(transform)
}

/** Raw cart: prices in rubles, nothing formatted. */
data class CartSnapshot(
    val lines: List<Line> = emptyList(),
    val deliveryId: String = DEFAULT_DELIVERY_ID,
    val isGiftWrapped: Boolean = false,
) {
    data class Line(
        val id: String,
        val title: String,
        val details: String,
        val unitPrice: Int,
        val quantity: Int,
        val isExtra: Boolean,
    )

    companion object {
        const val DEFAULT_DELIVERY_ID = "pickup"
    }
}
