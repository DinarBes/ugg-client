package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/** Variants of the «Button» component from the design. */
enum class UggButtonStyle {
    /** Main action of a screen: dark fill. */
    Primary,

    /** Secondary action: outline on the background. */
    Secondary,

    /** On top of photos and dark surfaces: white fill. */
    Light,
}

@Composable
fun UggButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: UggButtonStyle = UggButtonStyle.Primary,
    enabled: Boolean = true,
    showArrow: Boolean = true,
) {
    val colors = UggTheme.colors

    val (container, content) = when (style) {
        UggButtonStyle.Primary -> colors.surfaceInverse to colors.contentInverse
        UggButtonStyle.Secondary -> colors.background to colors.content
        UggButtonStyle.Light -> colors.surfaceElevated to colors.content
    }
    val border = if (style == UggButtonStyle.Secondary) BorderStroke(1.2.dp, colors.content) else null

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(UggTheme.spacing.buttonHeight)
            .alpha(if (enabled) 1f else 0.4f),
        shape = UggTheme.shapes.pill,
        color = container,
        contentColor = content,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = UggTheme.spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = text, style = UggTheme.typography.button, color = content)

            if (showArrow) {
                Icon(
                    imageVector = UggIcons.ArrowRight,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

