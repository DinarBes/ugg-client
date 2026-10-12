package com.ugg.client.kit.navigation.domain.external.entities

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

sealed class NavigationEvent {
    data class OnNavigate(
        val route: Destination,
        val block: NavBackStack<NavKey>.() -> Unit,
    ) : NavigationEvent()

    data object OnBack : NavigationEvent()
}
