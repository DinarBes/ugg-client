package com.ugg.client.kit.auth.presentation.internal.view

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import kotlinx.coroutines.delay

internal const val OTP_LENGTH = 6
private const val RESEND_DELAY_SECONDS = 60

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OtpView(
    phone: String,
    otp: String,
    error: Throwable?,
    isLoading: Boolean,
    isResending: Boolean,
    resendCount: Int,
    onOtpChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = UggTheme.colors
    val horizontal = UggTheme.spacing.xl

    // Restarts on every resend; survives rotation.
    var secondsLeft by rememberSaveable(resendCount) { mutableIntStateOf(RESEND_DELAY_SECONDS) }
    LaunchedEffect(resendCount) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
            Text(text = "Подтверждение".uppercase(), style = UggTheme.typography.overline, color = colors.accent)
            Text(text = "Введите код\nиз SMS", style = UggTheme.typography.displayL, color = colors.content)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Отправили на ${formatPhone(phone)}", style = UggTheme.typography.body, color = colors.contentSecondary)
                Text(
                    text = "Изменить",
                    style = UggTheme.typography.body,
                    color = colors.accentStrong,
                    modifier = Modifier.clip(UggTheme.shapes.small).clickable(onClick = onBack),
                )
            }

            Spacer(Modifier.height(UggTheme.spacing.s))

            OtpField(otp = otp, isError = error != null, onChange = onOtpChanged)

            Spacer(Modifier.height(UggTheme.spacing.xs))

            when {
                error != null -> Column(verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
                    Text(
                        text = error.message ?: "Неверный код",
                        style = UggTheme.typography.bodySmall,
                        color = colors.error,
                    )
                    ResendLink(isEnabled = secondsLeft == 0 && !isResending, secondsLeft = secondsLeft, onResend = onResend)
                }

                else -> ResendLink(isEnabled = secondsLeft == 0 && !isResending, secondsLeft = secondsLeft, onResend = onResend)
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
                text = if (isLoading) "Проверяем…" else "Подтвердить",
                onClick = onSubmit,
                enabled = otp.length == OTP_LENGTH && !isLoading,
                showArrow = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Код проверится сам, как только вы введёте последнюю цифру",
                style = UggTheme.typography.caption,
                color = colors.contentSecondary,
            )
        }
    }
}

@Composable
private fun ResendLink(isEnabled: Boolean, secondsLeft: Int, onResend: () -> Unit) {
    val colors = UggTheme.colors

    if (secondsLeft > 0) {
        Row(horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = UggIcons.Clock, contentDescription = null, tint = colors.contentSecondary, modifier = Modifier.size(16.dp))
            Text(
                text = "Отправить код повторно через ${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')}",
                style = UggTheme.typography.bodySmall,
                color = colors.contentSecondary,
            )
        }
    } else {
        Text(
            text = "Отправить код ещё раз",
            style = UggTheme.typography.label,
            color = if (isEnabled) colors.accentStrong else colors.contentSecondary,
            modifier = Modifier
                .clip(UggTheme.shapes.small)
                .clickable(enabled = isEnabled, onClick = onResend)
                .padding(vertical = 4.dp),
        )
    }
}

/**
 * One invisible text field drawn as six cells: the keyboard, paste and SMS autofill all go into a
 * single string instead of being juggled between six fields.
 */
@Composable
private fun OtpField(otp: String, isError: Boolean, onChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = otp,
        onValueChange = { input -> onChange(input.filter(Char::isDigit).take(OTP_LENGTH)) },
        modifier = Modifier.focusRequester(focusRequester),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = { innerTextField ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(OTP_LENGTH) { index ->
                    OtpCell(
                        digit = otp.getOrNull(index),
                        isActive = index == otp.length,
                        isError = isError,
                    )
                    if (index == OTP_LENGTH / 2 - 1) {
                        Box(Modifier.width(8.dp).height(2.dp).background(UggTheme.colors.outline))
                    }
                }
                // Still composed so the field keeps focus and input, just never seen.
                Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
            }
        },
    )
}

@Composable
private fun OtpCell(digit: Char?, isActive: Boolean, isError: Boolean) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.small

    val border = when {
        isError -> colors.error
        isActive -> colors.content
        else -> colors.outline
    }
    val background = when {
        isError -> colors.error.copy(alpha = 0.06f)
        digit != null -> colors.surfaceElevated
        else -> colors.surface
    }

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 58.dp)
            .clip(shape)
            .background(background)
            .border(if (isActive || isError) 1.5.dp else 1.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            digit != null -> Text(
                text = digit.toString(),
                style = UggTheme.typography.displayM.copy(fontSize = 28.sp),
                color = if (isError) colors.error else colors.content,
            )

            isActive -> Caret()
        }
    }
}

@Composable
private fun Caret() {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
    )

    Box(
        Modifier
            .width(2.dp)
            .height(24.dp)
            .alpha(alpha)
            .background(UggTheme.colors.content),
    )
}

/** «+79001234567» → «+7 900 123-45-67»; anything else is shown as is. */
private fun formatPhone(phone: String): String {
    val digits = phone.removePrefix("+7")
    if (!phone.startsWith("+7") || digits.length != 10) return phone

    return "+7 ${digits.substring(0, 3)} ${digits.substring(3, 6)}-${digits.substring(6, 8)}-${digits.substring(8)}"
}
