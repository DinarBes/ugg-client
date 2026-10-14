package com.ugg.client.kit.cart.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.cart.domain.exceptions.SignInRequiredException
import com.ugg.client.kit.cart.domain.states.CartState
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class CartViewModel(
    private val useCase: CartUseCase,
) : ViewModel() {
    val state: StateFlow<CartState>
        field = MutableStateFlow(CartState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@CartViewModel::class.toString() }

        state.update { it.copy(error = throwable) }
    }

    init {
        observe()
    }

    // Every action only writes; the new cart arrives through observe(), like any other change.
    fun addExtra(id: String) = viewModelScope.launch(unexpectedExceptionHandler) { useCase.addExtra(id) }

    fun changeQuantity(lineId: String, delta: Int) =
        viewModelScope.launch(unexpectedExceptionHandler) { useCase.changeQuantity(lineId, delta) }

    fun selectDelivery(id: String) = viewModelScope.launch(unexpectedExceptionHandler) { useCase.selectDelivery(id) }

    fun setGiftWrap(enabled: Boolean) = viewModelScope.launch(unexpectedExceptionHandler) { useCase.setGiftWrap(enabled) }

    /** «Оформить заказ». A guest is sent to sign in first; the cart waits for them. */
    fun checkout() = viewModelScope.launch(unexpectedExceptionHandler) {
        if (state.value.isCheckingOut) return@launch
        state.update { it.copy(isCheckingOut = true, error = null) }

        try {
            val number = useCase.checkout()
            state.update { it.copy(placedOrderNumber = number) }
        } catch (e: SignInRequiredException) {
            state.update { it.copy(isSignInRequired = true) }
        } finally {
            state.update { it.copy(isCheckingOut = false) }
        }
    }

    /** The screen showed «Заказ оформлен» or opened the phone step. */
    fun onCheckoutHandled() = state.update { it.copy(placedOrderNumber = null, isSignInRequired = false) }

    private fun observe() = viewModelScope.launch(unexpectedExceptionHandler) {
        useCase.observeCart().collect(::show)
    }

    private fun show(cart: Cart) = state.update { it.copy(cart = cart, error = null) }
}

@Suppress("unused")
fun DI.cartViewModel(): CartViewModel = this.direct.instance()
