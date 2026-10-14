package com.ugg.client.kit.profile.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.profile.domain.entities.Address
import com.ugg.client.kit.profile.domain.states.AddressFormState
import com.ugg.client.kit.profile.domain.useCases.AddressesUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddressFormViewModel(
    private val useCase: AddressesUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<AddressFormState>
        field = MutableStateFlow(AddressFormState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@AddressFormViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false, isSaving = false) }
    }

    /**
     * Starts the form over: called by the screen each time it opens, with its route argument;
     * `null` opens an empty form. (View models outlive the screen here, so nothing is reset by itself.)
     */
    fun load(addressId: String?) {
        state.update { AddressFormState() }
        if (addressId == null) return

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isLoading = true, error = null) }

            val address = useCase.address(addressId)
            state.update {
                if (address == null) it.copy(isLoading = false)
                else it.copy(address = address, isExisting = true, isLoading = false)
            }
        }
    }

    fun onChange(transform: (Address) -> Address) = state.update { it.copy(address = transform(it.address), error = null) }

    fun onSave() {
        if (state.value.isSaving) return
        if (!state.value.isValid) {
            state.update { it.copy(showErrors = true) }
            return
        }

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isSaving = true, showErrors = true, error = null) }

            useCase.save(state.value.address)

            state.update { it.copy(isSaving = false) }
            navigationContext.back()
        }
    }

    fun onDelete() {
        val id = state.value.address.id.takeIf { it.isNotEmpty() } ?: return

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isSaving = true, error = null) }

            useCase.delete(id)

            state.update { it.copy(isSaving = false) }
            navigationContext.back()
        }
    }
}
