package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalUggColors = staticCompositionLocalOf { LightUggColors }
private val LocalUggTypography = staticCompositionLocalOf<UggTypography> {
    error("UggTypography is not provided — wrap your content in UggTheme { … }")
}
private val LocalUggShapes = staticCompositionLocalOf { UggShapes() }
private val LocalUggSpacing = staticCompositionLocalOf { UggSpacing() }
private val LocalUggAdaptiveInfo = staticCompositionLocalOf<WindowAdaptiveInfo> {
    error("WindowAdaptiveInfo is not provided — wrap your content in UggTheme { … }")
}

/**
 * Root theme of the app. Light only for now.
 *
 * Provides the brand tokens ([UggTheme.colors], [UggTheme.typography], [UggTheme.shapes],
 * [UggTheme.spacing]) and also configures [MaterialTheme] with them, so Material 3 components
 * used as-is still look on-brand.
 *
 * The window size is measured here, once, and read through [UggTheme.adaptiveInfo] by the
 * composable that owns alternative layouts. [windowAdaptiveInfo] is a parameter so previews and
 * tests can pin a size.
 */
@Composable
fun UggTheme(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    content: @Composable () -> Unit,
) {
    val colors = LightUggColors
    val typography = uggTypography()
    val shapes = UggShapes()
    val spacing = UggSpacing()

    CompositionLocalProvider(
        LocalUggColors provides colors,
        LocalUggTypography provides typography,
        LocalUggShapes provides shapes,
        LocalUggSpacing provides spacing,
        LocalUggAdaptiveInfo provides windowAdaptiveInfo,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            shapes = shapes.toMaterialShapes(),
            content = content,
        )
    }
}

/** Access point for the current theme tokens: `UggTheme.colors.accent`, `UggTheme.typography.body`. */
object UggTheme {
    val colors: UggColors
        @Composable @ReadOnlyComposable
        get() = LocalUggColors.current

    val typography: UggTypography
        @Composable @ReadOnlyComposable
        get() = LocalUggTypography.current

    val shapes: UggShapes
        @Composable @ReadOnlyComposable
        get() = LocalUggShapes.current

    val spacing: UggSpacing
        @Composable @ReadOnlyComposable
        get() = LocalUggSpacing.current

    /**
     * Size of the window, for choosing a layout:
     * `UggTheme.adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)`.
     */
    val adaptiveInfo: WindowAdaptiveInfo
        @Composable @ReadOnlyComposable
        get() = LocalUggAdaptiveInfo.current
}
