package com.ugg.client.kit.onboarding.presentation.external.view.models

import com.ugg.client.kit.onboarding.domain.entities.OnboardingSlide
import com.ugg.client.kit.onboarding.domain.useCases.OnboardingUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchShowsSlides() = runTest {
        val vm = OnboardingViewModel(FakeUseCase())
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), vm.state.value.slides.map { it.id })
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = OnboardingViewModel(FakeUseCase(slidesError = Exception("stub error")))
        advanceUntilIdle()

        assertTrue(vm.state.value.slides.isEmpty())
        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun setPageUpdatesThePage() = runTest {
        val vm = OnboardingViewModel(FakeUseCase())

        vm.setPage(2)

        assertEquals(2, vm.state.value.page)
    }

    @Test
    fun finishRemembersCompletionBeforeLeaving() = runTest {
        val useCase = FakeUseCase()
        val vm = OnboardingViewModel(useCase)
        var completedWhenLeft: Boolean? = null

        vm.finish { completedWhenLeft = useCase.completed }
        advanceUntilIdle()

        assertEquals(true, completedWhenLeft)
    }

    @Test
    fun finishStaysWhenCompletionFails() = runTest {
        val vm = OnboardingViewModel(FakeUseCase(completeError = Exception("write failed")))
        var left = false

        vm.finish { left = true }
        advanceUntilIdle()

        assertFalse(left)
        assertEquals("write failed", vm.state.value.error?.message)
    }

    private class FakeUseCase(
        private val slidesError: Exception? = null,
        private val completeError: Exception? = null,
    ) : OnboardingUseCase {
        var completed = false

        override suspend fun slides(): List<OnboardingSlide> {
            slidesError?.let { throw it }

            return listOf(OnboardingSlide(id = "a"), OnboardingSlide(id = "b"))
        }

        override suspend fun isCompleted(): Boolean = completed

        override suspend fun complete() {
            completeError?.let { throw it }

            completed = true
        }
    }
}
