package com.ugg.client.kit.profile.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.profile.domain.states.NameState
import com.ugg.client.kit.profile.domain.useCases.SettingsUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Max length of the name; longer input is cut while typing. */
internal const val PROFILE_NAME_MAX_LENGTH = 40

class NameViewModel(
    private val useCase: SettingsUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<NameState>
        field = MutableStateFlow(NameState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@NameViewModel::class.toString() }

        state.update { it.copy(error = throwable, isSaving = false) }
    }

    /** Called by the screen each time it opens: the field starts with the saved name. */
    fun load() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { NameState() }
        val name = useCase.observeAccount().first()?.name.orEmpty()
        state.update { it.copy(name = name) }
    }

    fun onNameChange(name: String) = state.update { it.copy(name = name.take(PROFILE_NAME_MAX_LENGTH), error = null) }

    fun onSave() {
        if (!state.value.canSave) return

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isSaving = true, error = null) }

            useCase.saveName(state.value.name)

            state.update { it.copy(isSaving = false) }
            navigationContext.back()
        }
    }
}
