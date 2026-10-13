package com.ugg.client.kit.auth.presentation.internal.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

internal const val NATIONAL_NUMBER_LENGTH = 10

/**
 * Shows ten raw digits as «900 123-45-67» while the field keeps only the digits. A separator
 * appears only once the digit after it is typed, so the caret never sits behind a dangling dash.
 */
internal object PhoneVisualTransformation : VisualTransformation {
    /** Index of the digit after which a separator goes, and the separator. */
    private val separators = listOf(2 to ' ', 5 to '-', 7 to '-')

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val active = separators.filter { (index, _) -> digits.length > index + 1 }

        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                active.firstOrNull { it.first == index }?.let { append(it.second) }
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + active.count { (index, _) -> index < offset }

            override fun transformedToOriginal(offset: Int): Int {
                var original = 0
                var transformed = 0
                while (original < digits.length && transformed < offset) {
                    transformed += 1
                    if (active.any { it.first == original }) transformed += 1
                    original += 1
                }
                return original.coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
