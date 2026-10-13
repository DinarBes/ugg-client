package com.ugg.client.kit.auth.domain.states

data class InitiateState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
