package com.ugg.client.kit.story.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.story.domain.entities.Story
import com.ugg.client.kit.story.domain.states.StoryState
import com.ugg.client.kit.story.domain.useCases.StoryUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class StoryViewModel(
    private val useCase: StoryUseCase,
) : ViewModel() {
    val state: StateFlow<StoryState>
        field = MutableStateFlow(StoryState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@StoryViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    init {
        fetch()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            show(useCase.fetch())
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }

    private fun show(story: Story) = state.update { it.copy(story = story) }
}

@Suppress("unused")
fun DI.storyViewModel(): StoryViewModel = this.direct.instance()
