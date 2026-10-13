package com.ugg.client.kit.cart.domain.entities

/** What the product screen puts into the cart. */
data class CartProduct(
    /** Same id for the same model + options: adding it again raises the quantity. */
    val id: String,
    val title: String,
    /** «Каштан · 38 · Стандарт». */
    val details: String,
    /** Rubles. */
    val unitPrice: Int,
    /** ARGB of the chosen sheepskin, to show the photo in that shade. */
    val color: Long? = null,
)
