package com.ugg.client.kit.cart.presentation.external.view.models

import com.ugg.client.kit.cart.data.storage.CartStorage
import com.ugg.client.kit.cart.data.useCases.MockCartUseCase
import com.ugg.client.kit.cart.domain.entities.CartProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun startsWithAnEmptyCart() = runTest {
        val vm = CartViewModel(MockCartUseCase(CartStorage()))
        advanceUntilIdle()

        assertTrue(vm.state.value.cart?.isEmpty == true)
    }

    @Test
    fun seesWhatAnotherScreenAdded() = runTest {
        val storage = CartStorage()
        val vm = CartViewModel(MockCartUseCase(storage))
        advanceUntilIdle()

        MockCartUseCase(storage).addProduct(CartProduct("classic", "Классика", "Каштан · 38", 14900))
        advanceUntilIdle()

        assertEquals(1, vm.state.value.cart?.itemCount)
    }

    @Test
    fun actionsUpdateTheCart() = runTest {
        val vm = CartViewModel(MockCartUseCase(CartStorage()))
        advanceUntilIdle()

        vm.addExtra("linen-bag")
        vm.setGiftWrap(true)
        advanceUntilIdle()

        val cart = vm.state.value.cart!!
        assertEquals(1, cart.itemCount)
        assertTrue(cart.isGiftWrapped)
    }
}
