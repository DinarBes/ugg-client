package com.ugg.client.kit.profile.presentation.external.view.models

import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.navigation.domain.external.entities.NavigationEvent
import com.ugg.client.kit.profile.data.useCases.MockAddressesUseCase
import com.ugg.client.kit.profile.data.useCases.MockSizeUseCase
import com.ugg.client.kit.profile.domain.entities.FootSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FormsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun sizeFormLoadsTheSavedSizeAndSavesTheChange() = runTest {
        val useCase = MockSizeUseCase()
        val navigation = NavigationContext()
        val vm = SizeViewModel(useCase, navigation)

        vm.load()
        advanceUntilIdle()
        assertEquals("38", vm.state.value.size)

        vm.onSelectSize("39")
        vm.onFootLengthChange("25.5")
        vm.onSave()
        advanceUntilIdle()

        assertEquals(FootSize("39", "standard", 25.5), useCase.current())
        assertIs<NavigationEvent.OnBack>(navigation.events.first())
    }

    @Test
    fun footLengthAcceptsOnlyANumber() = runTest {
        val vm = SizeViewModel(MockSizeUseCase(), NavigationContext())
        vm.load()
        advanceUntilIdle()

        vm.onFootLengthChange("24,5,3abc")
        assertEquals("24,5", vm.state.value.footLength)
        assertNull(vm.state.value.footLengthError)

        vm.onFootLengthChange("99")
        assertNotNull(vm.state.value.footLengthError)
        assertFalse(vm.state.value.canSave)
    }

    @Test
    fun addressFormShowsErrorsOnlyAfterTryingToSave() = runTest {
        val vm = AddressFormViewModel(MockAddressesUseCase(), NavigationContext())
        vm.load(null)
        advanceUntilIdle()

        assertNull(vm.state.value.cityError)

        vm.onSave()
        advanceUntilIdle()

        assertEquals("Укажите город", vm.state.value.cityError)
        assertEquals("Укажите улицу и дом", vm.state.value.streetError)
    }

    @Test
    fun newAddressIsSavedAndTheFormCloses() = runTest {
        val useCase = MockAddressesUseCase()
        val navigation = NavigationContext()
        val vm = AddressFormViewModel(useCase, navigation)
        vm.load(null)

        vm.onChange { it.copy(city = "Листвянка", street = "ул. Береговая, 3") }
        vm.onSave()
        advanceUntilIdle()

        assertTrue(useCase.observeAddresses().first().any { it.city == "Листвянка" })
        assertIs<NavigationEvent.OnBack>(navigation.events.first())
    }

    @Test
    fun openingAnotherAddressStartsTheFormOver() = runTest {
        val vm = AddressFormViewModel(MockAddressesUseCase(), NavigationContext())
        vm.load("home")
        advanceUntilIdle()
        assertTrue(vm.state.value.isExisting)

        vm.load(null)
        advanceUntilIdle()

        assertFalse(vm.state.value.isExisting)
        assertEquals("", vm.state.value.address.city)
    }
}
