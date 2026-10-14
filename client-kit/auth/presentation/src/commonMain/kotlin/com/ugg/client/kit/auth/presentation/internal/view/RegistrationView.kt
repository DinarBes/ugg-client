package com.ugg.client.kit.auth.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

@Composable
internal fun RegistrationView(
    name: String,
    isSaving: Boolean,
    canSubmit: Boolean,
    error: Throwable?,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
) {
    val colors = UggTheme.colors
    val horizontal = UggTheme.spacing.xl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            // safeDrawing includes the keyboard: the button stays above it while typing.
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        UggIconButton(
            icon = UggIcons.ArrowLeft,
            contentDescription = "Пропустить",
            onClick = onSkip,
            modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontal, vertical = UggTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            Text(text = "Знакомство".uppercase(), style = UggTheme.typography.overline, color = colors.accent)
            Text(text = "Как к вам\nобращаться?", style = UggTheme.typography.displayL, color = colors.content)
            Text(
                text = "Мастер увидит имя в вашем заказе — так мы не перепутаем пары и знаем, кого ждать в мастерской.",
                style = UggTheme.typography.body,
                color = colors.contentSecondary,
            )

            Spacer(Modifier.height(UggTheme.spacing.xs))

            Text(text = "Имя", style = UggTheme.typography.caption, color = colors.contentSecondary)
            NameField(
                name = name,
                isError = error != null,
                onChange = onNameChange,
                onDone = { if (canSubmit) onSubmit() },
            )

            if (error != null) {
                Text(
                    text = error.message ?: "Не получилось сохранить. Попробуйте ещё раз.",
                    style = UggTheme.typography.bodySmall,
                    color = colors.error,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontal, vertical = UggTheme.spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            UggButton(
                text = if (isSaving) "Сохраняем…" else "Готово",
                onClick = onSubmit,
                enabled = canSubmit,
                showArrow = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Пропустить",
                style = UggTheme.typography.label,
                color = colors.accentStrong,
                modifier = Modifier
                    .clip(UggTheme.shapes.small)
                    .clickable(onClick = onSkip)
                    .padding(horizontal = UggTheme.spacing.s, vertical = UggTheme.spacing.xs),
            )
        }
    }
}

@Composable
private fun NameField(
    name: String,
    isError: Boolean,
    onChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.medium
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> colors.error
        isFocused -> colors.content
        else -> colors.outline
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = name,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = UggTheme.typography.heading.copy(color = colors.content),
        cursorBrush = SolidColor(colors.content),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(shape)
                    .background(colors.surfaceElevated)
                    .border(if (isFocused || isError) 1.5.dp else 1.dp, borderColor, shape)
                    .padding(horizontal = UggTheme.spacing.m),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (name.isEmpty()) {
                    Text(text = "Анна", style = UggTheme.typography.heading, color = colors.contentSecondary.copy(alpha = 0.5f))
                }
                innerTextField()
            }
        },
    )
}
