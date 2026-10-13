package com.ugg.client.kit.auth.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

@Composable
internal fun InitiateView(
    nationalNumber: String,
    countryCode: String,
    error: Throwable?,
    isLoading: Boolean,
    onNationalNumberChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    val colors = UggTheme.colors
    val horizontal = UggTheme.spacing.xl
    val isComplete = nationalNumber.length == NATIONAL_NUMBER_LENGTH
    val canSubmit = isComplete && !isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            // safeDrawing includes the keyboard: the button stays above it while typing.
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        UggIconButton(
            icon = UggIcons.ArrowLeft,
            contentDescription = "Назад",
            onClick = onBack,
            modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontal, vertical = UggTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        ) {
            Text(text = "Вход".uppercase(), style = UggTheme.typography.overline, color = colors.accent)
            Text(text = "Ваш номер\nтелефона", style = UggTheme.typography.displayL, color = colors.content)
            Text(
                text = "Пришлём код в SMS. По номеру сохраним ваши заказы и сообщим, когда пара будет готова.",
                style = UggTheme.typography.body,
                color = colors.contentSecondary,
            )

            Spacer(Modifier.height(UggTheme.spacing.xs))

            Text(text = "Телефон", style = UggTheme.typography.caption, color = colors.contentSecondary)
            PhoneField(
                countryCode = countryCode,
                nationalNumber = nationalNumber,
                isError = error != null,
                onChange = onNationalNumberChanged,
                onDone = { if (canSubmit) onSubmit() },
            )

            if (error != null) {
                Text(
                    text = error.message ?: "Не удалось отправить код. Попробуйте ещё раз.",
                    style = UggTheme.typography.bodySmall,
                    color = colors.error,
                )
            } else {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
                ) {
                    Icon(
                        imageVector = UggIcons.ShieldCheck,
                        contentDescription = null,
                        tint = colors.contentSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Номер нужен только для заказов — без рассылок и звонков.",
                        style = UggTheme.typography.caption,
                        color = colors.contentSecondary,
                    )
                }
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
                text = if (isLoading) "Отправляем код…" else "Получить код",
                onClick = onSubmit,
                enabled = canSubmit,
                showArrow = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            TermsText(onOpenTerms = onOpenTerms, onOpenPrivacy = onOpenPrivacy)
            Text(
                text = "Продолжить без входа",
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
private fun PhoneField(
    countryCode: String,
    nationalNumber: String,
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

    // The number is the only thing to do here — open the keyboard straight away.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = nationalNumber,
        onValueChange = { input -> onChange(input.filter(Char::isDigit).take(NATIONAL_NUMBER_LENGTH)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = UggTheme.typography.heading.copy(color = colors.content),
        cursorBrush = SolidColor(colors.content),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = PhoneVisualTransformation,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(shape)
                    .background(colors.surfaceElevated)
                    .border(if (isFocused || isError) 1.5.dp else 1.dp, borderColor, shape)
                    .padding(horizontal = UggTheme.spacing.m),
                horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = countryCode, style = UggTheme.typography.heading, color = colors.content)
                Box(Modifier.width(1.dp).height(24.dp).background(colors.outline))
                Box(modifier = Modifier.weight(1f)) {
                    if (nationalNumber.isEmpty()) {
                        Text(text = "900 000-00-00", style = UggTheme.typography.heading, color = colors.contentSecondary.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun TermsText(onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit) {
    val linkStyle = TextLinkStyles(SpanStyle(color = UggTheme.colors.accentStrong))

    val text = buildAnnotatedString {
        append("Нажимая «Получить код», вы соглашаетесь с ")
        withLink(LinkAnnotation.Clickable("terms", linkStyle) { onOpenTerms() }) { append("условиями") }
        append("\nи ")
        withLink(LinkAnnotation.Clickable("privacy", linkStyle) { onOpenPrivacy() }) { append("политикой конфиденциальности") }
    }

    Text(
        text = text,
        style = UggTheme.typography.caption,
        color = UggTheme.colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
}
