package com.ugg.client.kit.story.domain.states

import com.ugg.client.kit.story.domain.entities.Story

data class StoryState(
    val story: Story? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
