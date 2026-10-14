package com.ugg.client.kit.home.domain.states

import com.ugg.client.kit.home.domain.entities.Home

data class HomeState(
    val home: Home? = null,
    /** Units in the cart — the badge on the bag icon. */
    val cartCount: Int = 0,
    val isLoading: Boolean = false,
    /** Pull-to-refresh in progress: the content stays on screen under the spinner. */
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)
