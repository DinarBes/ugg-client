package com.ugg.client.kit.splash.domain.entities

import com.ugg.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class SplashDestination : Destination() {
    @Serializable
    data object Splash : SplashDestination() {
        override val isTabViewVisible = false
    }
}