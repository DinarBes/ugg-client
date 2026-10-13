package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors of the app. Screens read these through [UggTheme.colors].
 *
 * Only a light scheme exists for now; a dark one is another instance of this class.
 */
@Immutable
data class UggColors(
    /** Screen background. */
    val background: Color,
    /** Cards, chips, inactive segments. */
    val surface: Color,
    /** Photo placeholders, highlighted hint blocks. */
    val surfaceVariant: Color,
    /** Raised surfaces: cards with an outline, bottom bars. */
    val surfaceElevated: Color,
    /** Dark surfaces: primary button, order status card, care banner. */
    val surfaceInverse: Color,

    /** Primary text and icons. */
    val content: Color,
    /** Secondary text, inactive tab items. */
    val contentSecondary: Color,
    /** Text and icons placed on [surfaceInverse] or on photos. */
    val contentInverse: Color,

    /** Brand accent: overlines, links, active indicators. */
    val accent: Color,
    /** Prices, icons inside feature cards. */
    val accentStrong: Color,
    /** Positive / informational statuses: «в наличии», «открыто». */
    val info: Color,
    val error: Color,

    /** Outlines and dividers. */
    val outline: Color,
)

val LightUggColors = UggColors(
    background = UggPalette.Milk,
    surface = UggPalette.Cream,
    surfaceVariant = UggPalette.Sand,
    surfaceElevated = UggPalette.White,
    surfaceInverse = UggPalette.Espresso,
    content = UggPalette.Espresso,
    contentSecondary = UggPalette.Taupe,
    contentInverse = UggPalette.Milk,
    accent = UggPalette.Camel,
    accentStrong = UggPalette.Tobacco,
    info = UggPalette.Baikal,
    error = UggPalette.Terracotta,
    outline = UggPalette.Line,
)

/** Maps our roles onto Material 3 so stock components (TextField, Switch, dialogs) match the brand. */
internal fun UggColors.toMaterialColorScheme(): ColorScheme = lightColorScheme(
    primary = surfaceInverse,
    onPrimary = contentInverse,
    primaryContainer = surfaceVariant,
    onPrimaryContainer = content,
    secondary = accent,
    onSecondary = UggPalette.White,
    secondaryContainer = surface,
    onSecondaryContainer = content,
    tertiary = info,
    onTertiary = UggPalette.White,
    background = background,
    onBackground = content,
    surface = background,
    onSurface = content,
    surfaceVariant = surface,
    onSurfaceVariant = contentSecondary,
    surfaceContainerLowest = surfaceElevated,
    surfaceContainerLow = background,
    surfaceContainer = surface,
    surfaceContainerHigh = surface,
    surfaceContainerHighest = surfaceVariant,
    inverseSurface = surfaceInverse,
    inverseOnSurface = contentInverse,
    outline = outline,
    outlineVariant = outline,
    error = error,
    onError = UggPalette.White,
)
