package com.ugg.client.kit.cart.domain.entities

import com.ugg.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class CartFlow : Destination() {
    @Serializable
    data object Cart : CartFlow() {
        override val isTabViewVisible = false
    }
}
