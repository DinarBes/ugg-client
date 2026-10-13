package com.ugg.client.kit.profile.presentation.external.view.models

import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
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
class ProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchShowsProfile() = runTest {
        val vm = ProfileViewModel(SuccessUseCase())
        advanceUntilIdle()

        assertEquals("Анна", vm.state.value.profile?.name)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = ProfileViewModel(FailureUseCase())
        advanceUntilIdle()

        assertNull(vm.state.value.profile)
        assertEquals("stub error", vm.state.value.error?.message)
    }

    private class SuccessUseCase : ProfileUseCase {
        override suspend fun fetch() = Profile(name = "Анна")
    }

    private class FailureUseCase : ProfileUseCase {
        override suspend fun fetch(): Profile = throw Exception("stub error")
    }
}
