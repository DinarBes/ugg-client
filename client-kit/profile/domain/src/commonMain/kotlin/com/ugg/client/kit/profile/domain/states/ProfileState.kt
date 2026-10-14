package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Profile

data class ProfileState(
    val profile: Profile? = null,
    /** Nobody is signed in: the screen offers to sign in instead of the profile. */
    val isGuest: Boolean = false,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
