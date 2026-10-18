package com.ugg.client.kit.profile.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.profile.domain.entities.ProfileFlow
import com.ugg.client.kit.profile.domain.states.AddressesState
import com.ugg.client.kit.profile.domain.useCases.AddressesUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class AddressesViewModel(
    private val useCase: AddressesUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<AddressesState>
        field = MutableStateFlow(AddressesState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@AddressesViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false, isRefreshing = false) }
    }

    private var observing: Job? = null

    init {
        observe()
    }

    /** Live: an address saved on the form shows up here as soon as the form closes. */
    fun observe() = subscribe(isRefresh = false)

    /**
     * Pull-to-refresh. The data is live already (a Firestore listener); this restarts the
     * subscription, so a broken one (network was lost, rules changed) gets a fresh start.
     */
    fun refresh() {
        if (state.value.isRefreshing) return
        // Set right away, not inside the coroutine: a second pull before it starts is ignored.
        state.update { it.copy(isRefreshing = true) }
        subscribe(isRefresh = true)
    }

    private fun subscribe(isRefresh: Boolean) {
        observing?.cancel()
        observing = viewModelScope.launch(unexpectedExceptionHandler) {
            if (!isRefresh) state.update { it.copy(isLoading = true, error = null) }

            useCase.observeAddresses().collect { addresses ->
                state.update { it.copy(addresses = addresses, isLoading = false, isRefreshing = false, error = null) }
            }
        }
    }

    fun onAdd() = navigationContext.tryNavigate(ProfileFlow.AddressForm(addressId = null))

    fun onOpen(id: String) = navigationContext.tryNavigate(ProfileFlow.AddressForm(addressId = id))
}

@Suppress("unused")
fun DI.addressesViewModel(): AddressesViewModel = this.direct.instance()
