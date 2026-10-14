package com.ugg.client.kit.care.domain.states

import com.ugg.client.kit.care.domain.entities.Care

data class CareState(
    val care: Care? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
