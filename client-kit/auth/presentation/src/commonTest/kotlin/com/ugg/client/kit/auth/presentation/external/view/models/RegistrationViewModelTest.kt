package com.ugg.client.kit.auth.presentation.external.view.models

import com.ugg.client.kit.auth.domain.useCases.RegistrationUseCase
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.navigation.domain.external.entities.NavigationEvent
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun savingTheNameClosesTheStep() = runTest {
        val useCase = RecordingUseCase()
        val navigation = NavigationContext()
        val vm = RegistrationViewModel(useCase, navigation)

        vm.onNameChange("Анна")
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals("Анна", useCase.saved)
        assertIs<NavigationEvent.OnBack>(navigation.events.first())
    }

    @Test
    fun blankNameCannotBeSubmitted() = runTest {
        val useCase = RecordingUseCase()
        val vm = RegistrationViewModel(useCase, NavigationContext())

        vm.onNameChange("   ")
        vm.onSubmit()
        advanceUntilIdle()

        assertFalse(vm.state.value.canSubmit)
        assertNull(useCase.saved)
    }

    @Test
    fun longNameIsCut() = runTest {
        val vm = RegistrationViewModel(RecordingUseCase(), NavigationContext())

        vm.onNameChange("А".repeat(100))

        assertEquals(NAME_MAX_LENGTH, vm.state.value.name.length)
    }

    @Test
    fun failureKeepsTheStepOpenWithTheError() = runTest {
        val vm = RegistrationViewModel(
            object : RegistrationUseCase {
                override suspend fun saveName(name: String) = throw Exception("stub error")
            },
            NavigationContext(),
        )

        vm.onNameChange("Анна")
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isSaving)
    }

    private class RecordingUseCase : RegistrationUseCase {
        var saved: String? = null
        override suspend fun saveName(name: String) { saved = name }
    }
}
