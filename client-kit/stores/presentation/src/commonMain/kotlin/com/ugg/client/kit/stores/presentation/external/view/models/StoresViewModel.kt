package com.ugg.client.kit.stores.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.stores.domain.entities.Store
import com.ugg.client.kit.stores.domain.states.StoresState
import com.ugg.client.kit.stores.domain.useCases.StoresUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class StoresViewModel(
    private val useCase: StoresUseCase,
) : ViewModel() {
    val state: StateFlow<StoresState>
        field = MutableStateFlow(StoresState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@StoresViewModel::class.toString() }

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

    /** `null` shows every city. A selected store hidden by the filter is deselected. */
    fun selectCity(city: String?) = state.update { current ->
        val filtered = current.copy(city = city)
        val isSelectionVisible = filtered.visibleStores.any { it.id == current.selectedStoreId }

        filtered.copy(selectedStoreId = current.selectedStoreId.takeIf { isSelectionVisible })
    }

    fun selectStore(id: String?) = state.update { it.copy(selectedStoreId = id) }

    private fun show(stores: List<Store>) = state.update { current ->
        current.copy(
            stores = stores,
            city = current.city?.takeIf { city -> stores.any { it.city == city } },
            selectedStoreId = current.selectedStoreId?.takeIf { id -> stores.any { it.id == id } },
        )
    }
}

@Suppress("unused")
fun DI.storesViewModel(): StoresViewModel = this.direct.instance()
