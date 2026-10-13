package com.ugg.client.kit.home.domain.states

import com.ugg.client.kit.home.domain.entities.Home

data class HomeState(
    val home: Home? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
