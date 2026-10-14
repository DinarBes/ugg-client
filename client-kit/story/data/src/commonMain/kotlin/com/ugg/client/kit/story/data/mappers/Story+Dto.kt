package com.ugg.client.kit.story.data.mappers

import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.kit.story.domain.entities.Story
import com.ugg.client.kit.story.domain.entities.StoryHighlight
import com.ugg.client.kit.story.domain.entities.StoryStep

internal fun StoryContentDto.toDomain() = Story(
    kicker = kicker,
    title = title,
    intro = intro,
    stepsTitle = stepsTitle,
    steps = steps.map { StoryStep(number = it.number, title = it.title, description = it.description) },
    // An empty card in the console means «don't show it».
    highlight = highlight
        ?.takeIf { it.title.isNotBlank() || it.body.isNotBlank() }
        ?.let { StoryHighlight(kicker = it.kicker, title = it.title, body = it.body) },
)
