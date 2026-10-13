package com.ugg.client.kit.home.presentation.external.view.models

import com.ugg.client.kit.home.domain.entities.Home
import com.ugg.client.kit.home.domain.entities.HomeHero
import com.ugg.client.kit.home.domain.useCases.HomeUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchShowsHome() = runTest {
        val vm = HomeViewModel(SuccessUseCase())
        advanceUntilIdle()

        assertEquals("Классика", vm.state.value.home?.hero?.title)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = HomeViewModel(FailureUseCase())
        advanceUntilIdle()

        assertNull(vm.state.value.home)
        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun retryAfterFailureClearsTheError() = runTest {
        val useCase = FlakyUseCase()
        val vm = HomeViewModel(useCase)
        advanceUntilIdle()

        vm.fetch()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals("Классика", vm.state.value.home?.hero?.title)
    }

    private class SuccessUseCase : HomeUseCase {
        override suspend fun fetch() = Home(hero = HomeHero(title = "Классика"))
    }

    private class FailureUseCase : HomeUseCase {
        override suspend fun fetch(): Home = throw Exception("stub error")
    }

    private class FlakyUseCase : HomeUseCase {
        private var calls = 0

        override suspend fun fetch(): Home {
            if (calls++ == 0) throw Exception("first call fails")

            return Home(hero = HomeHero(title = "Классика"))
        }
    }
}
