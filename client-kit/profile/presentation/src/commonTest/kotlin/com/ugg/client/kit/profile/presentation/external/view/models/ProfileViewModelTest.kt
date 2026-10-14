package com.ugg.client.kit.profile.presentation.external.view.models

import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
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
class ProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun showsProfileOfSignedInUser() = runTest {
        val vm = ProfileViewModel(FakeUseCase(Profile(name = "Анна")))
        advanceUntilIdle()

        assertEquals("Анна", vm.state.value.profile?.name)
        assertFalse(vm.state.value.isGuest)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun signingOutShowsGuest() = runTest {
        val vm = ProfileViewModel(FakeUseCase(Profile(name = "Анна")))
        advanceUntilIdle()

        vm.signOut()
        advanceUntilIdle()

        assertNull(vm.state.value.profile)
        assertTrue(vm.state.value.isGuest)
    }

    @Test
    fun errorIsShownAndRetrySubscribesAgain() = runTest {
        var fail = true
        val useCase = object : ProfileUseCase {
            override fun observeProfile(): Flow<Profile?> = flow {
                if (fail) throw Exception("stub error") else emit(Profile(name = "Анна"))
            }
            override suspend fun signOut() = Unit
        }
        val vm = ProfileViewModel(useCase)
        advanceUntilIdle()
        assertEquals("stub error", vm.state.value.error?.message)

        fail = false
        vm.observe()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals("Анна", vm.state.value.profile?.name)
    }

    @Test
    fun refreshResubscribesAndStops() = runTest {
        var subscriptions = 0
        val useCase = object : ProfileUseCase {
            override fun observeProfile(): Flow<Profile?> = flow {
                subscriptions++
                emit(Profile(name = "Анна #$subscriptions"))
            }
            override suspend fun signOut() = Unit
        }
        val vm = ProfileViewModel(useCase)
        advanceUntilIdle()

        vm.refresh()
        assertTrue(vm.state.value.isRefreshing)
        advanceUntilIdle()

        assertEquals(2, subscriptions)
        assertEquals("Анна #2", vm.state.value.profile?.name)
        assertFalse(vm.state.value.isRefreshing)
    }

    private class FakeUseCase(profile: Profile?) : ProfileUseCase {
        private val profile = MutableStateFlow(profile)
        override fun observeProfile(): Flow<Profile?> = profile
        override suspend fun signOut() = profile.update { null }
    }
}
