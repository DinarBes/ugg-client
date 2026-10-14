package com.ugg.client.kit.story.data.useCases

import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.story.data.mappers.toDomain
import com.ugg.client.kit.story.domain.entities.Story
import com.ugg.client.kit.story.domain.useCases.StoryUseCase

/** «О мастерской» from Firestore, `content/story`. */
class SharedStoryUseCase(
    private val catalog: CatalogService,
) : StoryUseCase {

    override suspend fun fetch(): Story = translatingContentErrors { catalog.story().toDomain() }
}
