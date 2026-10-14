package com.ugg.client.kit.splash.domain.entities

import com.ugg.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class SplashFlow : Destination() {
    /** First launch: the user has not seen the onboarding yet. */
    @Serializable
    data object Onboarding : SplashFlow() {
        override val isTabViewVisible = false
    }

    @Serializable
    data object Auth : SplashFlow() {
        override val isTabViewVisible = false
    }

    @Serializable
    data object Launch : SplashFlow() {
        override val isTabViewVisible = false
    }

    /** Right after the first sign-in: «Как к вам обращаться?», shown over the app. */
    @Serializable
    data object Registration : SplashFlow() {
        override val isTabViewVisible = false
    }
}