package com.ugg.client.kit.cart.domain.entities

data class CartLine(
    val id: String = "",
    val title: String = "",
    val details: String = "",
    /** Line total, formatted: «14 900 ₽». */
    val price: String = "",
    val quantity: Int = 1,
    /** Accessory from «Добавить к заказу» rather than the boots themselves. */
    val isExtra: Boolean = false,
)
