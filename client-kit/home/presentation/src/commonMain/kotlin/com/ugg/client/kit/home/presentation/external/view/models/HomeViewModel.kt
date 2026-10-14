package com.ugg.client.kit.home.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
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
    private val cartUseCase: CartUseCase,
) : ViewModel() {
    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@HomeViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    init {
        fetch()
        observeCart()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            show(useCase.fetch())
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Pull-to-refresh: reads the data again while the current content stays on screen. A failure
     * keeps that content — the error screen shows only when there was nothing to show yet.
     */
    fun refresh() {
        if (state.value.isRefreshing) return
        // Set right away, not inside the coroutine: a second pull before it starts is ignored.
        state.update { it.copy(isRefreshing = true) }

        viewModelScope.launch(unexpectedExceptionHandler) {
            try {
                show(useCase.fetch())
                state.update { it.copy(error = null) }
            } finally {
                state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun observeCart() = viewModelScope.launch(unexpectedExceptionHandler) {
        cartUseCase.observeCart().collect { cart -> state.update { it.copy(cartCount = cart.itemCount) } }
    }

    private fun show(home: Home) = state.update { it.copy(home = home) }
}

@Suppress("unused")
fun DI.homeViewModel(): HomeViewModel = this.direct.instance()
