package com.ugg.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.auth.presentation.external.view.models.OtpViewModel
import com.ugg.client.kit.auth.presentation.internal.view.OtpView
import org.kodein.di.compose.viewmodel.rememberViewModel

@Composable
fun OtpScreen(
    phone: String,
    sessionId: String,
) {
    val viewModel: OtpViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    OtpView(
        otp = state.otp,
        error = state.error,
        isLoading = state.isLoading,
        onOtpChanged = { viewModel.onStateChange(state.copy(otp = it)) },
        onSubmit = { viewModel.onSubmit(sessionId) }
    )
}
