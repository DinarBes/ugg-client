package com.ugg.client.kit.product.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.cart.domain.entities.CartProduct
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.states.ProductState
import com.ugg.client.kit.product.domain.useCases.ProductUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ProductViewModel(
    private val useCase: ProductUseCase,
    private val cartUseCase: CartUseCase,
) : ViewModel() {
    val state: StateFlow<ProductState>
        field = MutableStateFlow(ProductState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@ProductViewModel::class.toString() }

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

    fun selectShade(id: String) = state.update { it.copy(selection = it.selection.copy(shadeId = id)) }

    fun selectHeight(id: String) = state.update { it.copy(selection = it.selection.copy(heightId = id)) }

    fun selectSize(label: String) = state.update { current ->
        val isAvailable = current.product?.sizes?.any { it.label == label && it.isAvailable } == true

        if (isAvailable) current.copy(selection = current.selection.copy(size = label)) else current
    }

    /** Opens [index], or collapses it if it is already open — one block at a time. */
    fun toggleDetail(index: Int) = state.update {
        it.copy(expandedDetail = if (it.expandedDetail == index) null else index)
    }

    /**
     * Puts the current selection into the cart, then calls [next] (the host opens the cart).
     * Does nothing until a size is picked — the button is disabled then anyway.
     */
    fun addToCart(next: () -> Unit) = viewModelScope.launch(unexpectedExceptionHandler) {
        val current = state.value
        val product = current.product ?: return@launch
        val selection = current.selection.takeIf { it.isComplete } ?: return@launch

        val shade = product.shades.firstOrNull { it.id == selection.shadeId }
        val height = product.heights.firstOrNull { it.id == selection.heightId }

        cartUseCase.addProduct(
            CartProduct(
                // The same model with the same options is one cart line: adding it again raises the quantity.
                id = listOf(product.id, selection.shadeId, selection.heightId, selection.size).joinToString("-"),
                title = product.title,
                details = listOfNotNull(shade?.name, selection.size, height?.name).joinToString(" · "),
                unitPrice = product.priceValue,
            ),
        )

        next()
    }

    /**
     * Color and height start on the first option so the user only has to pick a size; a choice made
     * before a refetch survives as long as the option still exists.
     */
    private fun show(product: Product) = state.update { current ->
        val selection = current.selection

        current.copy(
            product = product,
            selection = selection.copy(
                shadeId = selection.shadeId?.takeIf { id -> product.shades.any { it.id == id } }
                    ?: product.shades.firstOrNull()?.id,
                heightId = selection.heightId?.takeIf { id -> product.heights.any { it.id == id } }
                    ?: product.heights.firstOrNull()?.id,
                size = selection.size?.takeIf { label -> product.sizes.any { it.label == label && it.isAvailable } },
            ),
        )
    }
}

@Suppress("unused")
fun DI.productViewModel(): ProductViewModel = this.direct.instance()
