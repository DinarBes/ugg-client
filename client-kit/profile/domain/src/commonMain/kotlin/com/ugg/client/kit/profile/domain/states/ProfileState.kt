package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Profile

data class ProfileState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
