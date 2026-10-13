package com.ugg.client.infrastructure.firebase.firestore.dto

import kotlinx.serialization.Serializable

/** `products/{id}`. Prices are rubles, colors are `"#RRGGBB"`. */
@Serializable
data class ProductDto(
    val id: String = "",
    val title: String = "",
    val kicker: String = "",
    val description: String = "",
    val price: Int = 0,
    val leadTimeDays: LeadTimeDto = LeadTimeDto(),
    /** Paths in Firebase Storage. */
    val photos: List<String> = emptyList(),
    val shades: List<ShadeDto> = emptyList(),
    val heights: List<HeightDto> = emptyList(),
    val sizes: List<SizeDto> = emptyList(),
    val customSizeHint: String? = null,
    val features: List<FeatureDto> = emptyList(),
    val details: List<DetailDto> = emptyList(),
) {
    @Serializable
    data class LeadTimeDto(val min: Int = 0, val max: Int = 0)

    @Serializable
    data class ShadeDto(
        val id: String = "",
        val name: String = "",
        val color: String = "",
        val inStock: Boolean = true,
    )

    @Serializable
    data class HeightDto(val id: String = "", val name: String = "", val heightCm: Int = 0)

    @Serializable
    data class SizeDto(val label: String = "", val available: Boolean = true)

    /** [kind]: `material`, `handmade`, `warmth`, `warranty` — the app picks the icon from it. */
    @Serializable
    data class FeatureDto(val kind: String = "", val title: String = "", val caption: String = "")

    @Serializable
    data class DetailDto(val title: String = "", val body: String = "")
}
