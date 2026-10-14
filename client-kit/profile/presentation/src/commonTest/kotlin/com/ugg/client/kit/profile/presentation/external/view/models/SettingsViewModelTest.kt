package com.ugg.client.kit.profile.presentation.external.view.models

import com.ugg.client.kit.core.domain.AppInfo
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.navigation.domain.external.entities.NavigationEvent
import com.ugg.client.kit.profile.data.useCases.MockSettingsUseCase
import com.ugg.client.kit.profile.domain.entities.ProfileFlow
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
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun showsTheAccountAndTheVersion() = runTest {
        val vm = SettingsViewModel(MockSettingsUseCase(), AppInfo("1.0", 1), NavigationContext())
        advanceUntilIdle()

        assertEquals("Анна", vm.state.value.account?.name)
        assertEquals("Версия 1.0 (1)", vm.state.value.version)
    }

    @Test
    fun deletingTheAccountLeavesAGuest() = runTest {
        val vm = SettingsViewModel(MockSettingsUseCase(), AppInfo(), NavigationContext())
        advanceUntilIdle()

        vm.onDeleteAccount()
        advanceUntilIdle()

        assertNull(vm.state.value.account)
        assertEquals("", vm.state.value.version)
    }

    @Test
    fun editingTheNameOpensItsScreen() = runTest {
        val navigation = NavigationContext()
        val vm = SettingsViewModel(MockSettingsUseCase(), AppInfo(), navigation)

        vm.onEditName()

        val event = navigation.events.first()
        assertIs<NavigationEvent.OnNavigate>(event)
        assertEquals(ProfileFlow.Name, event.route)
    }

    @Test
    fun nameScreenStartsWithTheSavedNameAndSavesTheNewOne() = runTest {
        val useCase = MockSettingsUseCase()
        val navigation = NavigationContext()
        val vm = NameViewModel(useCase, navigation)

        vm.load()
        advanceUntilIdle()
        assertEquals("Анна", vm.state.value.name)

        vm.onNameChange("Аня")
        vm.onSave()
        advanceUntilIdle()

        assertEquals("Аня", useCase.observeAccount().first()?.name)
        assertIs<NavigationEvent.OnBack>(navigation.events.first())
    }
}
