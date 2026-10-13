package com.ugg.client.kit.home.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.home.domain.entities.Home
import com.ugg.client.kit.home.domain.states.HomeState
import com.ugg.client.kit.home.domain.useCases.HomeUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class HomeViewModel(
    private val useCase: HomeUseCase,
) : ViewModel() {
    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@HomeViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    init {
        fetch()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            show(useCase.fetch())
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }

    private fun show(home: Home) = state.update { it.copy(home = home) }
}

@Suppress("unused")
fun DI.homeViewModel(): HomeViewModel = this.direct.instance()
