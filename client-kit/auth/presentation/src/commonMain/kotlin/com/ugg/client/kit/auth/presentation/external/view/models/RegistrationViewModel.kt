package com.ugg.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.auth.domain.states.RegistrationState
import com.ugg.client.kit.auth.domain.useCases.RegistrationUseCase
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.instance
import org.kodein.di.direct
import org.kodein.di.DI

/** Max length of the name; longer input is cut while typing. */
internal const val NAME_MAX_LENGTH = 40

class RegistrationViewModel(
    private val registrationUseCase: RegistrationUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<RegistrationState>
        field = MutableStateFlow(RegistrationState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@RegistrationViewModel::class.toString() }

        state.update { it.copy(error = throwable, isSaving = false) }
    }

    fun onNameChange(name: String) = state.update { it.copy(name = name.take(NAME_MAX_LENGTH), error = null) }

    fun onSubmit() {
        if (!state.value.canSubmit) return

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isSaving = true, error = null) }

            registrationUseCase.saveName(state.value.name)

            state.update { it.copy(isSaving = false) }
            // The step sits over the app: closing it lands on the tabs.
            navigationContext.back()
        }
    }

    fun onSkip() = navigationContext.back()
}

@Suppress("unused")
fun DI.registrationViewModel(): RegistrationViewModel = this.direct.instance()
