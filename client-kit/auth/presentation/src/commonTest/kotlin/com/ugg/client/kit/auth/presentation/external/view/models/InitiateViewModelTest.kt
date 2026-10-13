package com.ugg.client.kit.auth.presentation.external.view.models

import com.ugg.client.kit.auth.domain.entities.AuthFlow
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.auth.domain.states.InitiateState
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitiateViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialStateIsEmpty() = runTest {
        val vm = InitiateViewModel(SuccessUseCase(), NavigationContext())

        assertEquals(InitiateState(), vm.state.value)
    }

    @Test
    fun onSubmitNavigatesToOtpCarryingPhoneAndSessionId() = runTest {
        val navigation = NavigationContext()
        val vm = InitiateViewModel(SuccessUseCase(), navigation)
        vm.onStateChange(vm.state.value.copy(phone = PHONE))

        vm.onSubmit()
        advanceUntilIdle()

        val event = navigation.events.first()
        assertTrue(event is NavigationEvent.OnNavigate)

        // Данные шага передаются навигационным аргументом, а не общим контекстом.
        val destination = event.route as AuthFlow.Otp
        assertEquals(PHONE, destination.phone)
        assertEquals(SESSION_ID, destination.sessionId)

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun onSubmitSetsErrorOnFailure() = runTest {
        val vm = InitiateViewModel(FailureUseCase(), NavigationContext())

        vm.onSubmit()
        advanceUntilIdle()

        assertEquals("network error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    private class SuccessUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = SESSION_ID
        override suspend fun login(otp: String, sessionId: String) = Unit
    }

    private class FailureUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = throw Exception("network error")
        override suspend fun login(otp: String, sessionId: String) = Unit
    }

    private companion object {
        const val PHONE = "79001234567"
        const val SESSION_ID = "stub-session-id"
    }
}
