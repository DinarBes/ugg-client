package com.ugg.client.kit.product.data.mappers

import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.kit.core.data.formatting.plural
import com.ugg.client.kit.core.data.formatting.rubles
import com.ugg.client.kit.core.data.formatting.toArgb
import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.entities.ProductDetail
import com.ugg.client.kit.product.domain.entities.ProductFeature
import com.ugg.client.kit.product.domain.entities.ProductHeight
import com.ugg.client.kit.product.domain.entities.ProductShade
import com.ugg.client.kit.product.domain.entities.ProductSize

internal fun ProductDto.toDomain() = Product(
    id = id,
    kicker = kicker,
    title = title,
    price = price.rubles(),
    priceValue = price,
    leadTime = leadTimeDays.toLeadTime(),
    description = description,
    photoCount = photos.size,
    shades = shades.map { ProductShade(id = it.id, name = it.name, color = it.color.toArgb()) },
    heights = heights.map { ProductHeight(id = it.id, name = it.name, value = "${it.heightCm} см") },
    sizes = sizes.map { ProductSize(label = it.label, isAvailable = it.available) },
    customSizeHint = customSizeHint?.takeIf { it.isNotBlank() },
    features = features.map { ProductFeature(kind = it.kind.toFeatureKind(), title = it.title, caption = it.caption) },
    details = details.map { ProductDetail(title = it.title, body = it.body) },
)

/** {5, 7} → «Сошьём за 5–7 дней». */
private fun ProductDto.LeadTimeDto.toLeadTime(): String {
    if (max <= 0) return ""
    val days = if (min in 1 until max) "$min–$max" else "$max"
    return "Сошьём за $days ${max.plural("день", "дня", "дней")}"
}

/** Unknown kinds get the sheepskin icon rather than breaking the screen. */
private fun String.toFeatureKind() = when (this) {
    "handmade" -> ProductFeature.Kind.Handmade
    "warmth" -> ProductFeature.Kind.Warmth
    "warranty" -> ProductFeature.Kind.Warranty
    else -> ProductFeature.Kind.Material
}
