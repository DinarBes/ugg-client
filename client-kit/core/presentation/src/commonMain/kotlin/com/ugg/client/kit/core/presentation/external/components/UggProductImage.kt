package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import com.ugg.client.kit.core.presentation.resources.Res
import com.ugg.client.kit.core.presentation.resources.uggs
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs

/**
 * Cut-out photo of the model on a transparent background — place it on any surface.
 *
 * There is one photo, in the sand sheepskin. Pass [color] to preview another shade: the photo
 * is recolored, keeping its light and shade. A color close to sand leaves it untouched.
 */
@Composable
fun UggProductImage(
    modifier: Modifier = Modifier,
    color: Color? = null,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
) {
    val painter = painterResource(Res.drawable.uggs)
    val colorFilter = remember(color) { color?.takeUnless { it.isPhotoColor() }?.let(::recolor) }

    // Without an explicit ratio a bitmap is laid out at its pixel size, so a small photo stays
    // small even under fillMaxWidth. Locking the ratio lets the width set the size.
    val size = painter.intrinsicSize
    val ratio = if (size.isSpecified && size.height > 0f) size.width / size.height else 1f

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.aspectRatio(ratio),
        alignment = alignment,
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter,
    )
}

/** Sheepskin tone of the photo itself; recoloring it into the same tone only muddies it. */
private val PhotoColor = Color(0xFFD7BF9C)

private fun Color.isPhotoColor(): Boolean {
    val photo = PhotoColor

    return abs(red - photo.red) + abs(green - photo.green) + abs(blue - photo.blue) < 0.12f
}

/**
 * Grayscale, then scaled into [target]: the suede takes the new color and keeps its texture.
 * The gain lifts mid-gray suede to roughly the target's own brightness.
 */
private fun recolor(target: Color): ColorFilter {
    val gain = 1.35f
    val r = target.red * gain
    val g = target.green * gain
    val b = target.blue * gain

    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.299f * r, 0.587f * r, 0.114f * r, 0f, 0f,
                0.299f * g, 0.587f * g, 0.114f * g, 0f, 0f,
                0.299f * b, 0.587f * b, 0.114f * b, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}
