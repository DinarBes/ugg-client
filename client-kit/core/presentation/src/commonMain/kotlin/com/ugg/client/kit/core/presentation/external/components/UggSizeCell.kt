package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * «Size Cell» from the design. A disabled cell is a size that cannot be ordered right now: it is
 * struck through and does not react to taps.
 */
@Composable
fun UggSizeCell(
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors

    val container = when {
        isSelected -> colors.surfaceInverse
        !isEnabled -> colors.surface
        else -> colors.surfaceElevated
    }
    val content = when {
        isSelected -> colors.contentInverse
        !isEnabled -> colors.contentSecondary.copy(alpha = 0.45f)
        else -> colors.content
    }

    Surface(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.height(48.dp),
        shape = UggTheme.shapes.small,
        color = container,
        border = if (!isSelected && isEnabled) BorderStroke(1.dp, colors.outline) else null,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = UggTheme.typography.label,
                color = content,
                textDecoration = if (isEnabled) null else TextDecoration.LineThrough,
            )
        }
    }
}
