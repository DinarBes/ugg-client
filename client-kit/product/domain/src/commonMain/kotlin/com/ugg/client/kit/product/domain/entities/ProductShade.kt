package com.ugg.client.kit.product.domain.entities

data class ProductShade(
    val id: String = "",
    val name: String = "",
    /** ARGB, e.g. `0xFFB98A5E`. */
    val color: Long = 0xFFB98A5E,
)
