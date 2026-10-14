package com.ugg.client.kit.home.presentation.external.view.models

import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.cart.domain.entities.CartProduct
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import com.ugg.client.kit.home.domain.entities.Home
import com.ugg.client.kit.home.domain.entities.HomeHero
import com.ugg.client.kit.home.domain.useCases.HomeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchShowsHome() = runTest {
        val vm = HomeViewModel(SuccessUseCase(), FakeCartUseCase())
        advanceUntilIdle()

        assertEquals("Классика", vm.state.value.home?.hero?.title)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = HomeViewModel(FailureUseCase(), FakeCartUseCase())
        advanceUntilIdle()

        assertNull(vm.state.value.home)
        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun retryAfterFailureClearsTheError() = runTest {
        val useCase = FlakyUseCase()
        val vm = HomeViewModel(useCase, FakeCartUseCase())
        advanceUntilIdle()

        vm.fetch()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals("Классика", vm.state.value.home?.hero?.title)
    }

    @Test
    fun refreshShowsTheNewContent() = runTest {
        val useCase = SequenceUseCase(listOf(Result.success("Классика"), Result.success("Классика 2")))
        val vm = HomeViewModel(useCase, FakeCartUseCase())
        advanceUntilIdle()

        vm.refresh()
        advanceUntilIdle()

        assertEquals("Классика 2", vm.state.value.home?.hero?.title)
        assertFalse(vm.state.value.isRefreshing)
    }

    @Test
    fun failedRefreshKeepsTheContentOnScreen() = runTest {
        val useCase = SequenceUseCase(listOf(Result.success("Классика"), Result.failure(Exception("offline"))))
        val vm = HomeViewModel(useCase, FakeCartUseCase())
        advanceUntilIdle()

        vm.refresh()
        advanceUntilIdle()

        assertEquals("Классика", vm.state.value.home?.hero?.title)
        assertFalse(vm.state.value.isRefreshing)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun cartCountFollowsTheCart() = runTest {
        val vm = HomeViewModel(SuccessUseCase(), FakeCartUseCase(itemCount = 2))
        advanceUntilIdle()

        assertEquals(2, vm.state.value.cartCount)
    }

    private class FakeCartUseCase(private val itemCount: Int = 0) : CartUseCase {
        override fun observeCart(): Flow<Cart> = flowOf(Cart(itemCount = itemCount))
        override suspend fun addProduct(product: CartProduct) = Unit
        override suspend fun addExtra(id: String) = Unit
        override suspend fun changeQuantity(lineId: String, delta: Int) = Unit
        override suspend fun selectDelivery(id: String) = Unit
        override suspend fun setGiftWrap(enabled: Boolean) = Unit
        override suspend fun checkout(): Long = 1024
    }

    private class SuccessUseCase : HomeUseCase {
        override suspend fun fetch() = Home(hero = HomeHero(title = "Классика"))
    }

    private class FailureUseCase : HomeUseCase {
        override suspend fun fetch(): Home = throw Exception("stub error")
    }

    /** Answers each fetch with the next result: a title or a failure. */
    private class SequenceUseCase(private val results: List<Result<String>>) : HomeUseCase {
        private var calls = 0

        override suspend fun fetch(): Home = Home(hero = HomeHero(title = results[calls++].getOrThrow()))
    }

    private class FlakyUseCase : HomeUseCase {
        private var calls = 0

        override suspend fun fetch(): Home {
            if (calls++ == 0) throw Exception("first call fails")

            return Home(hero = HomeHero(title = "Классика"))
        }
    }
}
