package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Corner radii used in the design. */
@Immutable
data class UggShapes(
    /** 14 — size cells, thumbnails, segmented items. */
    val small: Shape = RoundedCornerShape(14.dp),
    /** 20 — feature cards, delivery options, small photos. */
    val medium: Shape = RoundedCornerShape(20.dp),
    /** 24 — content cards, map, banners. */
    val large: Shape = RoundedCornerShape(24.dp),
    /** 28 — hero photo, order status card. */
    val extraLarge: Shape = RoundedCornerShape(28.dp),
    /** Buttons, chips, round icon buttons. */
    val pill: Shape = CircleShape,
)

internal fun UggShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = small,
    medium = medium,
    large = large,
    extraLarge = extraLarge,
)
