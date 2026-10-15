package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale used in the design. */
@Immutable
data class UggSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val s: Dp = 12.dp,
    val m: Dp = 16.dp,
    val l: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 28.dp,
    /** Horizontal padding of every screen. */
    val screenHorizontal: Dp = 16.dp,
    /** Vertical gap between screen sections. */
    val section: Dp = 28.dp,
    /** Height of primary buttons. */
    val buttonHeight: Dp = 56.dp,
    /** Widest a single column of text and forms gets on a wide window. */
    val readableWidth: Dp = 640.dp,
    /** Widest a multi-column screen gets; beyond it the margins grow. */
    val wideContentWidth: Dp = 1200.dp,
)
