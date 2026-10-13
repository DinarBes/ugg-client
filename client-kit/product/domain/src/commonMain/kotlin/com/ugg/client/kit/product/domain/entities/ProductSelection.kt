package com.ugg.client.kit.product.domain.entities

/** What the user picked on the product screen — the future cart line. */
data class ProductSelection(
    val shadeId: String? = null,
    val heightId: String? = null,
    val size: String? = null,
) {
    val isComplete: Boolean
        get() = shadeId != null && heightId != null && size != null
}
