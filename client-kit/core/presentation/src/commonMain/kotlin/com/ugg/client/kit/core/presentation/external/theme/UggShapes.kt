package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/** Corner radii used in the design. */
@Immutable
data class UggShapes(
    /** 14 — size cells, thumbnails, segmented items. */
    val small: CornerBasedShape = RoundedCornerShape(14.dp),
    /** 20 — feature cards, delivery options, small photos. */
    val medium: CornerBasedShape = RoundedCornerShape(20.dp),
    /** 24 — content cards, map, banners. */
    val large: CornerBasedShape = RoundedCornerShape(24.dp),
    /** 28 — hero photo, order status card. */
    val extraLarge: CornerBasedShape = RoundedCornerShape(28.dp),
    /** Buttons, chips, round icon buttons. */
    val pill: CornerBasedShape = CircleShape,
)

internal fun UggShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = small,
    medium = medium,
    large = large,
    extraLarge = extraLarge,
)
