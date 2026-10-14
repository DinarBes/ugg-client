package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggSegment
import com.ugg.client.kit.core.presentation.external.components.UggSegmentedControl
import com.ugg.client.kit.core.presentation.external.components.UggSizeCell
import com.ugg.client.kit.core.presentation.external.components.UggTextField
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.states.SizeState

private const val SIZES_PER_ROW = 5

@Composable
internal fun SizeView(
    state: SizeState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectSize: (String) -> Unit,
    onSelectHeight: (String) -> Unit,
    onFootLengthChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val colors = UggTheme.colors
    val typography = UggTheme.typography

    FormScaffold(
        title = "Мой размер",
        onBack = onBack,
        isLoading = state.isLoading,
        // Only a failed load replaces the form; a failed save shows under it.
        loadError = state.error.takeIf { state.options.sizes.isEmpty() },
        onRetry = onRetry,
        bottom = {
            UggButton(
                text = if (state.isSaving) "Сохраняем…" else "Сохранить",
                onClick = onSave,
                enabled = state.canSave,
                showArrow = false,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Text(
            text = "Сохраним размер — в следующий раз он будет выбран сам, а мастер учтёт его при пошиве.",
            style = typography.body,
            color = colors.contentSecondary,
        )

        Text(text = "Размер", style = typography.heading, color = colors.content)
        Column(verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
            state.options.sizes.chunked(SIZES_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
                    row.forEach { size ->
                        UggSizeCell(
                            label = size,
                            isSelected = size == state.size,
                            isEnabled = true,
                            onClick = { onSelectSize(size) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(SIZES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        if (state.options.heights.isNotEmpty()) {
            Text(text = "Высота голенища", style = typography.heading, color = colors.content)
            UggSegmentedControl(
                segments = state.options.heights.map { UggSegment(it.name, it.value) },
                selectedIndex = state.options.heights.indexOfFirst { it.id == state.heightId },
                onSelect = { onSelectHeight(state.options.heights[it].id) },
            )
        }

        Text(text = "Длина стопы", style = typography.heading, color = colors.content)
        UggTextField(
            value = state.footLength,
            onValueChange = onFootLengthChange,
            label = "В сантиметрах, необязательно",
            placeholder = "24,5",
            error = state.footLengthError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        )
        MeasureTip()

        SaveError(state.error.takeIf { state.options.sizes.isNotEmpty() })
    }
}

@Composable
private fun MeasureTip() {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.medium)
            .background(colors.surface)
            .padding(UggTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(imageVector = UggIcons.Ruler, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(20.dp))
        Text(
            text = "Как измерить: встаньте на лист бумаги у стены, отметьте самый длинный палец и измерьте расстояние от стены до отметки. Если размера нет в сетке — сошьём по этой мерке.",
            style = UggTheme.typography.bodySmall,
            color = colors.contentSecondary,
        )
    }
}
