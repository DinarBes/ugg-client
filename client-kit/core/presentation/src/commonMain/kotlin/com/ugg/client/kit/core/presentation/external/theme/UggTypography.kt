package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ugg.client.kit.core.presentation.resources.Res
import com.ugg.client.kit.core.presentation.resources.cormorant_garamond_medium
import com.ugg.client.kit.core.presentation.resources.cormorant_garamond_medium_italic
import com.ugg.client.kit.core.presentation.resources.cormorant_garamond_semibold
import com.ugg.client.kit.core.presentation.resources.manrope_bold
import com.ugg.client.kit.core.presentation.resources.manrope_medium
import com.ugg.client.kit.core.presentation.resources.manrope_regular
import com.ugg.client.kit.core.presentation.resources.manrope_semibold
import org.jetbrains.compose.resources.Font

/**
 * Text styles from the Figma file (text styles «Display/…» and «Text/…»).
 *
 * Display styles use Cormorant Garamond, everything else Manrope.
 * [overline] is uppercase in the design: Compose has no text-case attribute, so call
 * `text.uppercase()` where you render it.
 */
@Immutable
data class UggTypography(
    /** 46/48 — screen heroes: «Классика», «Тепло, сшитое руками». */
    val displayXL: TextStyle,
    /** 34/38 — screen titles: «Корзина», «Наши точки». */
    val displayL: TextStyle,
    /** 26/30 — section titles: «Пять оттенков». */
    val displayM: TextStyle,
    /** 22/28 italic — quotes. */
    val displayItalic: TextStyle,
    /** 17/24 — card and list titles. */
    val heading: TextStyle,
    /** 15/23 — body copy. */
    val body: TextStyle,
    /** 13/19 — secondary copy, descriptions in cards. */
    val bodySmall: TextStyle,
    /** 14/20 — chips, menu rows, links. */
    val label: TextStyle,
    /** 12/16 — captions, tab labels. */
    val caption: TextStyle,
    /** 11/14, tracking 14%, uppercase — section kickers. */
    val overline: TextStyle,
    /** 20/26 — prices. */
    val price: TextStyle,
    /** 16/20 — button labels. */
    val button: TextStyle,
)

@Composable
internal fun cormorantGaramond(): FontFamily = FontFamily(
    Font(Res.font.cormorant_garamond_medium, FontWeight.Medium),
    Font(Res.font.cormorant_garamond_semibold, FontWeight.SemiBold),
    Font(Res.font.cormorant_garamond_medium_italic, FontWeight.Medium, FontStyle.Italic),
)

@Composable
internal fun manrope(): FontFamily = FontFamily(
    Font(Res.font.manrope_regular, FontWeight.Normal),
    Font(Res.font.manrope_medium, FontWeight.Medium),
    Font(Res.font.manrope_semibold, FontWeight.SemiBold),
    Font(Res.font.manrope_bold, FontWeight.Bold),
)

@Composable
internal fun uggTypography(): UggTypography {
    val display = cormorantGaramond()
    val text = manrope()

    return UggTypography(
        displayXL = TextStyle(fontFamily = display, fontWeight = FontWeight.Medium, fontSize = 46.sp, lineHeight = 48.sp, letterSpacing = (-0.01).em),
        displayL = TextStyle(fontFamily = display, fontWeight = FontWeight.Medium, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.005).em),
        displayM = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 30.sp),
        displayItalic = TextStyle(fontFamily = display, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 28.sp),
        heading = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
        body = TextStyle(fontFamily = text, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
        bodySmall = TextStyle(fontFamily = text, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
        label = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        caption = TextStyle(fontFamily = text, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        overline = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.14.em),
        price = TextStyle(fontFamily = text, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
        button = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    )
}

/** Maps our styles onto Material 3 slots so stock components pick up the brand fonts. */
internal fun UggTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = displayXL,
    displayMedium = displayL,
    displaySmall = displayM,
    headlineLarge = displayL,
    headlineMedium = displayM,
    headlineSmall = displayM,
    titleLarge = price,
    titleMedium = heading,
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = body,
    bodySmall = bodySmall,
    labelLarge = button,
    labelMedium = label,
    labelSmall = caption,
)
