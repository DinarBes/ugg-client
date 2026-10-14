package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

/**
 * A labelled text field in the app's style: caption above, rounded box, the error under it in
 * place of [hint]. Multi-line when [singleLine] is false (comments).
 */
@Composable
fun UggTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    hint: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.medium
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isError = error != null

    val borderColor = when {
        isError -> colors.error
        isFocused -> colors.content
        else -> colors.outline
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = UggTheme.typography.caption, color = colors.contentSecondary)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            textStyle = UggTheme.typography.body.copy(color = colors.content),
            cursorBrush = SolidColor(colors.content),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(shape)
                        .background(colors.surfaceElevated)
                        .border(if (isFocused || isError) 1.5.dp else 1.dp, borderColor, shape)
                        .padding(horizontal = UggTheme.spacing.m, vertical = 14.dp),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(text = placeholder, style = UggTheme.typography.body, color = colors.contentSecondary.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            },
        )

        when {
            error != null -> Text(text = error, style = UggTheme.typography.caption, color = colors.error)
            hint != null -> Text(text = hint, style = UggTheme.typography.caption, color = colors.contentSecondary)
        }
    }
}
