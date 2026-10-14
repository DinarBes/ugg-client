package com.ugg.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.auth.presentation.external.view.models.RegistrationViewModel
import com.ugg.client.kit.auth.presentation.internal.view.RegistrationView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Shown once, right after the first sign-in with a new number. Both «Готово» and «Пропустить»
 * close it; the name can be left empty.
 */
@Composable
fun RegistrationScreen() {
    val viewModel: RegistrationViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    RegistrationView(
        name = state.name,
        isSaving = state.isSaving,
        canSubmit = state.canSubmit,
        error = state.error,
        onNameChange = viewModel::onNameChange,
        onSubmit = viewModel::onSubmit,
        onSkip = viewModel::onSkip,
    )
}
