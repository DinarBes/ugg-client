package com.ugg.client.kit.care.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.care.domain.states.CareState
import com.ugg.client.kit.care.domain.useCases.CareUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CareViewModel(
    private val useCase: CareUseCase,
) : ViewModel() {
    val state: StateFlow<CareState>
        field = MutableStateFlow(CareState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@CareViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    init {
        fetch()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        val care = useCase.fetch()
        state.update { it.copy(care = care, isLoading = false) }
    }
}
