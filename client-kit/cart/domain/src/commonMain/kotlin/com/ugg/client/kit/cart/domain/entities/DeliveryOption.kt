package com.ugg.client.kit.cart.domain.entities

data class DeliveryOption(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    /** «Бесплатно», «от 450 ₽». */
    val price: String = "",
)
