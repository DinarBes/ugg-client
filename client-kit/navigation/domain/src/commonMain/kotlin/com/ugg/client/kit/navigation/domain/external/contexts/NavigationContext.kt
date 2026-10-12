package com.ugg.client.kit.navigation.domain.external.contexts

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.ugg.client.kit.navigation.domain.external.entities.Destination
import com.ugg.client.kit.navigation.domain.external.entities.NavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NavigationContext {
    val events: SharedFlow<NavigationEvent>
        field = MutableSharedFlow(replay = 1)

    fun tryNavigate(route: Destination, block: NavBackStack<NavKey>.() -> Unit = {}) {
        events.tryEmit(NavigationEvent.OnNavigate(route, block))
    }

    suspend fun navigate(route: Destination, block: NavBackStack<NavKey>.() -> Unit = {}) {
        events.emit(NavigationEvent.OnNavigate(route, block))
    }

    fun back() {
        events.tryEmit(NavigationEvent.OnBack)
    }
}
