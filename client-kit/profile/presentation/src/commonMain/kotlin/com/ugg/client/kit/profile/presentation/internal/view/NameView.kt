package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggTextField
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.states.NameState

@Composable
internal fun NameView(
    state: NameState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    FormScaffold(
        title = "Имя",
        onBack = onBack,
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
            text = "Так к вам будет обращаться мастер, и это имя увидит в заказе.",
            style = UggTheme.typography.body,
            color = UggTheme.colors.contentSecondary,
        )
        UggTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = "Как к вам обращаться",
            placeholder = "Анна",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
        )
        SaveError(state.error)
    }
}
