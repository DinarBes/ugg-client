package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/** «Color Chip» from the design: swatch + name, the selected one gets a dark outline. */
@Composable
fun UggColorChip(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = UggTheme.shapes.pill,
        color = if (isSelected) colors.surfaceElevated else colors.surface,
        border = if (isSelected) BorderStroke(1.2.dp, colors.content) else null,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 14.dp, top = UggTheme.spacing.xs, bottom = UggTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(UggTheme.shapes.pill)
                    .background(color)
                    .border(1.dp, colors.content.copy(alpha = 0.12f), UggTheme.shapes.pill),
            )
            Text(text = name, style = UggTheme.typography.label, color = colors.content)
        }
    }
}
