package com.ugg.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.auth.presentation.external.view.models.InitiateViewModel
import com.ugg.client.kit.auth.presentation.internal.view.InitiateView
import org.kodein.di.compose.viewmodel.rememberViewModel

@Composable
fun InitiateScreen() {
    val viewModel: InitiateViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    InitiateView(
        phone = state.phone,
        error = state.error,
        isLoading = state.isLoading,
        onPhoneChanged = { viewModel.onStateChange(state.copy(phone = it)) },
        onSubmit = { viewModel.onSubmit() },
    )
}
