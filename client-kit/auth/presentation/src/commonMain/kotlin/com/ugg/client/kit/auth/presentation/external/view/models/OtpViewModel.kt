package com.ugg.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.auth.domain.entities.PhoneChallenge
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.auth.domain.states.OtpState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class OtpViewModel(
    private val authUseCase: AuthUseCase,
) : ViewModel() {
    val state: StateFlow<OtpState>
        field = MutableStateFlow(OtpState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@OtpViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    fun onStateChange(new: OtpState) = state.update { new }

    /** A clean code step for a new number: no digits, no error, no session of the previous attempt. */
    fun reset() = state.update { OtpState() }

    fun onSubmit(sessionId: String) = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            // A resent code belongs to a new session; the navigation argument is only the first one.
            authUseCase.login(state.value.otp, state.value.sessionId ?: sessionId)
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }

    /** Asks for a fresh code to the same [phone]; the typed digits are cleared. */
    fun resend(phone: String) = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isResending = true, otp = "", error = null) }

        try {
            when (val challenge = authUseCase.initiate(phone)) {
                is PhoneChallenge.CodeSent ->
                    state.update { it.copy(sessionId = challenge.sessionId, resendCount = it.resendCount + 1) }
                // Verified without a code this time; the splash takes the app forward.
                PhoneChallenge.SignedIn -> Unit
            }
        } finally {
            state.update { it.copy(isResending = false) }
        }
    }
}

@Suppress("unused")
fun DI.otpViewModel(): OtpViewModel = this.direct.instance()
