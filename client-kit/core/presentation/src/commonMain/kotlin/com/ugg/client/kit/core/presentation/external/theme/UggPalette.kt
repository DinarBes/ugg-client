package com.ugg.client.kit.core.presentation.external.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw brand colors from the Figma file «Байкал — угги ручной работы» (collection «Палитра»).
 *
 * Do not use these directly in screens — go through [UggTheme.colors], which gives each color a
 * role. The palette exists so the roles have one place to point at.
 */
object UggPalette {
    /** Фон приложения. */
    val Milk = Color(0xFFFBF8F3)

    /** Карточки, поверхности. */
    val Cream = Color(0xFFF3ECE1)

    /** Плашки фото, разделители. */
    val Sand = Color(0xFFE7DACA)

    /** Акцент: цвет овчины. */
    val Camel = Color(0xFFB98A5E)

    /** Тёмный акцент, цены. */
    val Tobacco = Color(0xFF7A4F33)

    /** Основной текст, кнопки. */
    val Espresso = Color(0xFF2A211C)

    /** Вторичный текст. */
    val Taupe = Color(0xFF8B7F75)

    /** Акцент «вода Байкала»: статусы «в наличии», «открыто». */
    val Baikal = Color(0xFF6F8C99)

    val White = Color(0xFFFFFFFF)

    /** Обводки и разделители. */
    val Line = Color(0xFFE6DDD1)

    /** Ошибки. Нет в макете — тёплый терракотовый в тон палитре. */
    val Terracotta = Color(0xFFB4533C)
}
