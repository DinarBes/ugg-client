package com.ugg.client.kit.cart.domain.entities

data class Cart(
    val lines: List<CartLine> = emptyList(),
    /** Accessories not in the cart yet. */
    val extras: List<CartExtra> = emptyList(),
    val deliveryOptions: List<DeliveryOption> = emptyList(),
    val selectedDeliveryId: String? = null,
    val isGiftWrapped: Boolean = false,
    val giftWrapTitle: String = "",
    val giftWrapDescription: String = "",
    val summary: List<CartSummaryRow> = emptyList(),
    /** «14 900 ₽». */
    val total: String = "",
    /** «1 товар · 14 900 ₽» under the title. */
    val subtitle: String = "",
    /** Units across all lines — the badge on the bag icon. */
    val itemCount: Int = 0,
) {
    val isEmpty: Boolean
        get() = lines.isEmpty()
}
