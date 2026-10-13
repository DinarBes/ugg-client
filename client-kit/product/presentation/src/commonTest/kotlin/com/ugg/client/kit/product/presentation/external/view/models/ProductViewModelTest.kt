package com.ugg.client.kit.product.presentation.external.view.models

import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.cart.domain.entities.CartProduct
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.entities.ProductHeight
import com.ugg.client.kit.product.domain.entities.ProductShade
import com.ugg.client.kit.product.domain.entities.ProductSize
import com.ugg.client.kit.product.domain.useCases.ProductUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val cart = FakeCartUseCase()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchPreselectsColorAndHeightButNotSize() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)
        advanceUntilIdle()

        val selection = vm.state.value.selection
        assertEquals("chestnut", selection.shadeId)
        assertEquals("standard", selection.heightId)
        assertNull(selection.size)
        assertFalse(selection.isComplete)
    }

    @Test
    fun pickingASizeCompletesTheSelection() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)
        advanceUntilIdle()

        vm.selectSize("38")

        assertEquals("38", vm.state.value.selection.size)
        assertTrue(vm.state.value.selection.isComplete)
    }

    @Test
    fun unavailableSizeCannotBePicked() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)
        advanceUntilIdle()

        vm.selectSize("44")

        assertNull(vm.state.value.selection.size)
    }

    @Test
    fun choicesSurviveARefetch() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)
        advanceUntilIdle()
        vm.selectShade("sand")
        vm.selectSize("38")

        vm.fetch()
        advanceUntilIdle()

        assertEquals("sand", vm.state.value.selection.shadeId)
        assertEquals("38", vm.state.value.selection.size)
    }

    @Test
    fun toggleDetailOpensOneBlockAtATime() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)

        vm.toggleDetail(2)
        assertEquals(2, vm.state.value.expandedDetail)

        vm.toggleDetail(2)
        assertNull(vm.state.value.expandedDetail)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = ProductViewModel(FailureUseCase(), cart)
        advanceUntilIdle()

        assertNull(vm.state.value.product)
        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun addToCartPutsTheSelectionInAndThenLeaves() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)
        advanceUntilIdle()
        vm.selectSize("38")
        var addedWhenLeft: CartProduct? = null

        vm.addToCart { addedWhenLeft = cart.added.lastOrNull() }
        advanceUntilIdle()

        assertEquals("classic-chestnut-standard-38", addedWhenLeft?.id)
        assertEquals("Каштан · 38 · Стандарт", addedWhenLeft?.details)
        assertEquals(14900, addedWhenLeft?.unitPrice)
    }

    @Test
    fun addToCartWithoutASizeDoesNothing() = runTest {
        val vm = ProductViewModel(SuccessUseCase(), cart)
        advanceUntilIdle()
        var left = false

        vm.addToCart { left = true }
        advanceUntilIdle()

        assertFalse(left)
        assertTrue(cart.added.isEmpty())
    }

    private class FakeCartUseCase : CartUseCase {
        val added = mutableListOf<CartProduct>()

        override fun observeCart(): Flow<Cart> = emptyFlow()
        override suspend fun addProduct(product: CartProduct) { added += product }
        override suspend fun addExtra(id: String) = Unit
        override suspend fun changeQuantity(lineId: String, delta: Int) = Unit
        override suspend fun selectDelivery(id: String) = Unit
        override suspend fun setGiftWrap(enabled: Boolean) = Unit
    }

    private class SuccessUseCase : ProductUseCase {
        override suspend fun fetch() = Product(
            id = "classic",
            title = "Классика",
            priceValue = 14900,
            shades = listOf(ProductShade("chestnut", "Каштан"), ProductShade("sand", "Песок")),
            heights = listOf(ProductHeight("standard", "Стандарт", "22 см")),
            sizes = listOf(ProductSize("38"), ProductSize("44", isAvailable = false)),
        )
    }

    private class FailureUseCase : ProductUseCase {
        override suspend fun fetch(): Product = throw Exception("stub error")
    }
}
