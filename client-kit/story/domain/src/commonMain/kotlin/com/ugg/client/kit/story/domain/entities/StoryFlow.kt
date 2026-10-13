package com.ugg.client.kit.story.domain.entities

import com.ugg.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class StoryFlow : Destination() {
    /** «История мастерской», opened from the home screen. */
    @Serializable
    data object Story : StoryFlow() {
        override val isTabViewVisible = false
    }
}
