package com.ugg.client.kit.product.domain.entities

data class ProductFeature(
    val kind: Kind = Kind.Material,
    val title: String = "",
    val caption: String = "",
) {
    /** What the feature is about; the View picks the icon from it. */
    enum class Kind { Material, Handmade, Warmth, Warranty }
}
