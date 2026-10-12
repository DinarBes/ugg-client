package com.ugg.client.kit.navigation.domain.external.entities

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
abstract class Destination : NavKey {
    abstract val isTabViewVisible: Boolean
}
