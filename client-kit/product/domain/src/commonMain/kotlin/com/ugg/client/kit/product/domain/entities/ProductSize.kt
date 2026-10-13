package com.ugg.client.kit.product.domain.entities

data class ProductSize(
    /** «38». */
    val label: String = "",
    /** `false` — none in stock and no materials for this size right now. */
    val isAvailable: Boolean = true,
)
