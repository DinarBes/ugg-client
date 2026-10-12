package com.ugg.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.auth.presentation.external.states.OtpState
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

    fun onStateChange(new: OtpState) { state.value = new }

    fun onSubmit(sessionId: String) = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            authUseCase.login(state.value.otp, sessionId)
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }
}

@Suppress("unused")
fun DI.otpViewModel(): OtpViewModel = this.direct.instance()
