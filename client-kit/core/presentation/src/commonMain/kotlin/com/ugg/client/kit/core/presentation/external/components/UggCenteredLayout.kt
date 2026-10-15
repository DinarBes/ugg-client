package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * Keeps content at most [maxWidth] wide, centered, on any window — a no-op on a phone.
 *
 * Gives [content] the side [inset] instead of wrapping it in a narrower box, so a scrolling list
 * can stay full width (the mouse wheel and touch work over the margins too) and add the inset
 * to its own content padding.
 */
@Composable
fun UggCenteredLayout(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable (inset: Dp) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val inset = ((this.maxWidth - maxWidth) / 2).coerceAtLeast(0.dp)
        content(inset)
    }
}

/**
 * A screen held to [maxWidth] in the middle of the window, the margins in the app background —
 * for single-column screens that draw their own scrolling. A no-op on a phone.
 */
@Composable
fun UggCenteredBox(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().background(UggTheme.colors.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxSize()) {
            content()
        }
    }
}
