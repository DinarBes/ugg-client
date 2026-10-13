package com.ugg.client.kit.story.domain.useCases

import com.ugg.client.kit.story.domain.entities.Story

interface StoryUseCase {
    suspend fun fetch(): Story
}
