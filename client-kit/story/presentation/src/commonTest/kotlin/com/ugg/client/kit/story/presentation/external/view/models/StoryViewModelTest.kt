package com.ugg.client.kit.story.presentation.external.view.models

import com.ugg.client.kit.story.domain.entities.Story
import com.ugg.client.kit.story.domain.useCases.StoryUseCase
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
class StoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchShowsStory() = runTest {
        val vm = StoryViewModel(SuccessUseCase())
        advanceUntilIdle()

        assertEquals("О мастерской", vm.state.value.story?.kicker)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = StoryViewModel(FailureUseCase())
        advanceUntilIdle()

        assertNull(vm.state.value.story)
        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    private class SuccessUseCase : StoryUseCase {
        override suspend fun fetch() = Story(kicker = "О мастерской")
    }

    private class FailureUseCase : StoryUseCase {
        override suspend fun fetch(): Story = throw Exception("stub error")
    }
}
