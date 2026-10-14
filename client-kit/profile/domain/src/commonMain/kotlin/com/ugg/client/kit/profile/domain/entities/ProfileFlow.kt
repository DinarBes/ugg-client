package com.ugg.client.kit.profile.domain.entities

import com.ugg.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class ProfileFlow : Destination() {
    /** «Настройки»: name, account, app version. */
    @Serializable
    data object Settings : ProfileFlow() {
        override val isTabViewVisible = false
    }

    /** Changing the name, opened from the settings. */
    @Serializable
    data object Name : ProfileFlow() {
        override val isTabViewVisible = false
    }

    /** «Мой размер»: size, shaft height and the measured foot. */
    @Serializable
    data object Size : ProfileFlow() {
        override val isTabViewVisible = false
    }

    /** «Адреса доставки»: the saved addresses. */
    @Serializable
    data object Addresses : ProfileFlow() {
        override val isTabViewVisible = false
    }

    /** One address to add ([addressId] `null`) or edit. */
    @Serializable
    data class AddressForm(val addressId: String? = null) : ProfileFlow() {
        override val isTabViewVisible = false
    }
}
