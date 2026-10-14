package com.ugg.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.auth.presentation.external.view.models.OtpViewModel
import com.ugg.client.kit.auth.presentation.internal.view.OTP_LENGTH
import com.ugg.client.kit.auth.presentation.internal.view.OtpView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Code step of the sign-in. On success the splash flow takes the user into the app.
 *
 * @param phone full number from the previous step, «+79001234567».
 * @param onBack the back button and «Изменить» — both return to the phone step.
 */
@Composable
fun OtpScreen(
    phone: String,
    sessionId: String,
    onBack: () -> Unit,
) {
    val viewModel: OtpViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    // A fresh form on every visit to this step, but not on rotation or on «back» from the next
    // step: the flag lives as long as this back stack entry.
    var isStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isStarted) {
            isStarted = true
            viewModel.reset()
        }
    }

    OtpView(
        phone = phone,
        otp = state.otp,
        error = state.error,
        isLoading = state.isLoading,
        isResending = state.isResending,
        resendCount = state.resendCount,
        onOtpChanged = { otp ->
            viewModel.onStateChange(state.copy(otp = otp, error = null))

            // The last digit submits by itself — no reason to make the user reach for the button.
            if (otp.length == OTP_LENGTH && !state.isLoading) viewModel.onSubmit(sessionId)
        },
        onSubmit = { viewModel.onSubmit(sessionId) },
        onResend = { viewModel.resend(phone) },
        onBack = onBack,
    )
}
