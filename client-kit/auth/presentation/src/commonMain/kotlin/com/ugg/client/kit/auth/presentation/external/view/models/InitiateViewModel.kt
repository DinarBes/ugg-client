package com.ugg.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.auth.domain.entities.AuthFlow
import com.ugg.client.kit.auth.domain.entities.PhoneChallenge
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.auth.domain.states.InitiateState
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class InitiateViewModel(
    private val authUseCase: AuthUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<InitiateState>
        field = MutableStateFlow(InitiateState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@InitiateViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    fun onStateChange(new: InitiateState) = state.update { new }

    fun onSubmit() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            when (val challenge = authUseCase.initiate(state.value.phone)) {
                is PhoneChallenge.CodeSent -> navigationContext.navigate(AuthFlow.Otp(state.value.phone, challenge.sessionId))
                // Verified without a code: the splash sees the new user and takes the app forward.
                PhoneChallenge.SignedIn -> Unit
            }
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }
}

@Suppress("unused")
fun DI.initiateViewModel(): InitiateViewModel = this.direct.instance()
