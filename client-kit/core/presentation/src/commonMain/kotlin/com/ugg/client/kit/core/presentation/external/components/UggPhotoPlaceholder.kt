package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * Stand-in for a photo until real images arrive: a warm vertical gradient, light on top and
 * dark at the bottom, so copy placed over it stays readable the same way it will over a photo.
 *
 * Swap for an image loader (e.g. Coil) once the shots exist; the call sites keep their shape.
 */
@Composable
fun UggPhotoPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = UggTheme.shapes.large,
    top: Color = UggTheme.colors.surfaceVariant,
    bottom: Color = UggTheme.colors.accentStrong,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, bottom))),
        content = content,
    )
}
