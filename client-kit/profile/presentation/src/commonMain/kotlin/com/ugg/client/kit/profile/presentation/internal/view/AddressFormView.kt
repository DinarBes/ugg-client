package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggTextField
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.entities.Address
import com.ugg.client.kit.profile.domain.states.AddressFormState

@Composable
internal fun AddressFormView(
    state: AddressFormState,
    onBack: () -> Unit,
    onChange: ((Address) -> Address) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = UggTheme.colors
    val address = state.address
    val words = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)

    FormScaffold(
        title = if (state.isExisting) "Адрес" else "Новый адрес",
        onBack = onBack,
        isLoading = state.isLoading,
        bottom = {
            UggButton(
                text = if (state.isSaving) "Сохраняем…" else "Сохранить",
                onClick = onSave,
                enabled = !state.isSaving,
                showArrow = false,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isExisting) {
                Text(
                    text = "Удалить адрес",
                    style = UggTheme.typography.label,
                    color = colors.error,
                    modifier = Modifier
                        .clip(UggTheme.shapes.small)
                        .clickable(enabled = !state.isSaving, onClick = onDelete)
                        .padding(horizontal = UggTheme.spacing.s, vertical = UggTheme.spacing.xs),
                )
            }
        },
    ) {
        UggTextField(
            value = address.city,
            onValueChange = { value -> onChange { it.copy(city = value) } },
            label = "Город",
            placeholder = "Иркутск",
            error = state.cityError,
            keyboardOptions = words,
        )
        UggTextField(
            value = address.street,
            onValueChange = { value -> onChange { it.copy(street = value) } },
            label = "Улица и дом",
            placeholder = "ул. Ленина, 12",
            error = state.streetError,
            keyboardOptions = words,
        )
        UggTextField(
            value = address.flat,
            onValueChange = { value -> onChange { it.copy(flat = value) } },
            label = "Квартира или офис",
            placeholder = "Необязательно",
            keyboardOptions = words,
        )
        UggTextField(
            value = address.comment,
            onValueChange = { value -> onChange { it.copy(comment = value) } },
            label = "Комментарий для курьера",
            placeholder = "Подъезд, этаж, домофон",
            singleLine = false,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(UggTheme.shapes.medium)
                .clickable { onChange { it.copy(isDefault = !it.isDefault) } }
                .padding(vertical = UggTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Основной адрес", style = UggTheme.typography.label, color = colors.content)
                Text(text = "Подставим его при заказе с доставкой", style = UggTheme.typography.caption, color = colors.contentSecondary)
            }
            Switch(
                checked = address.isDefault,
                onCheckedChange = { checked -> onChange { it.copy(isDefault = checked) } },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.accent,
                    checkedThumbColor = colors.surfaceElevated,
                    uncheckedTrackColor = colors.surface,
                    uncheckedBorderColor = colors.outline,
                    uncheckedThumbColor = colors.contentSecondary,
                ),
            )
        }

        SaveError(state.error)
    }
}
