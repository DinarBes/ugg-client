package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/** Round 44 dp icon button from the design (header actions, back). */
@Composable
fun UggIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = UggTheme.colors.surface,
    hasBadge: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = UggTheme.shapes.pill,
        color = containerColor,
        contentColor = UggTheme.colors.content,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = UggTheme.colors.content,
                modifier = Modifier.size(22.dp),
            )

            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 9.dp, y = (-9).dp)
                        .size(9.dp)
                        .clip(UggTheme.shapes.pill)
                        .background(containerColor)
                        .padding(2.dp)
                        .clip(UggTheme.shapes.pill)
                        .background(UggTheme.colors.accent),
                )
            }
        }
    }
}
