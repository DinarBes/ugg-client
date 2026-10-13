package com.ugg.client.kit.auth.presentation.external.view.models

import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.auth.domain.states.OtpState
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
class OtpViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialStateIsEmpty() = runTest {
        val vm = OtpViewModel(SuccessUseCase())

        assertEquals(OtpState(), vm.state.value)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun onSubmitLogsInOnSuccess() = runTest {
        val vm = OtpViewModel(SuccessUseCase())
        vm.onStateChange(vm.state.value.copy(otp = "123456"))

        vm.onSubmit(SESSION_ID)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun onSubmitSetsErrorOnFailure() = runTest {
        val vm = OtpViewModel(FailureUseCase())
        vm.onStateChange(vm.state.value.copy(otp = "000000"))

        vm.onSubmit(SESSION_ID)
        advanceUntilIdle()

        assertEquals("Invalid OTP", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun onSubmitPassesTheSessionIdItWasGiven() = runTest {
        val useCase = RecordingUseCase()
        val vm = OtpViewModel(useCase)
        vm.onStateChange(vm.state.value.copy(otp = "123456"))

        vm.onSubmit(SESSION_ID)
        advanceUntilIdle()

        assertEquals(SESSION_ID, useCase.sessionId)
    }

    private class SuccessUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = SESSION_ID
        override suspend fun login(otp: String, sessionId: String) = Unit
    }

    private class FailureUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = SESSION_ID
        override suspend fun login(otp: String, sessionId: String) = throw Exception("Invalid OTP")
    }

    private class RecordingUseCase : AuthUseCase {
        var sessionId: String? = null

        override suspend fun initiate(phone: String): String = SESSION_ID
        override suspend fun login(otp: String, sessionId: String) { this.sessionId = sessionId }
    }

    private companion object {
        const val SESSION_ID = "stub-session-id"
    }
}
