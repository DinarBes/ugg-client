package com.ugg.client.kit.stores.presentation.external.view.models

import com.ugg.client.kit.stores.domain.entities.Store
import com.ugg.client.kit.stores.domain.useCases.StoresUseCase
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
class StoresViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchShowsEveryStore() = runTest {
        val vm = StoresViewModel(SuccessUseCase())
        advanceUntilIdle()

        assertEquals(listOf("irk", "lst"), vm.state.value.visibleStores.map { it.id })
        assertEquals(listOf("Иркутск", "Листвянка"), vm.state.value.cities)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun cityFilterNarrowsTheList() = runTest {
        val vm = StoresViewModel(SuccessUseCase())
        advanceUntilIdle()

        vm.selectCity("Листвянка")

        assertEquals(listOf("lst"), vm.state.value.visibleStores.map { it.id })
    }

    @Test
    fun filteringOutTheSelectedStoreDeselectsIt() = runTest {
        val vm = StoresViewModel(SuccessUseCase())
        advanceUntilIdle()
        vm.selectStore("irk")

        vm.selectCity("Листвянка")

        assertNull(vm.state.value.selectedStoreId)
    }

    @Test
    fun selectionSurvivesAFilterThatKeepsIt() = runTest {
        val vm = StoresViewModel(SuccessUseCase())
        advanceUntilIdle()
        vm.selectStore("irk")

        vm.selectCity("Иркутск")

        assertEquals("irk", vm.state.value.selectedStoreId)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = StoresViewModel(FailureUseCase())
        advanceUntilIdle()

        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    private class SuccessUseCase : StoresUseCase {
        override suspend fun fetch() = listOf(
            Store(id = "irk", city = "Иркутск"),
            Store(id = "lst", city = "Листвянка"),
        )
    }

    private class FailureUseCase : StoresUseCase {
        override suspend fun fetch(): List<Store> = throw Exception("stub error")
    }
}
