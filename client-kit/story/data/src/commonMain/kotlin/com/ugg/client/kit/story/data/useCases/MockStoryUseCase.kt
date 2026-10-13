package com.ugg.client.kit.story.data.useCases

import com.ugg.client.kit.story.domain.entities.Story
import com.ugg.client.kit.story.domain.useCases.StoryUseCase

/** Bundled content until the story is served from Firebase. */
class MockStoryUseCase : StoryUseCase {
    override suspend fun fetch(): Story = Story.mock
}
