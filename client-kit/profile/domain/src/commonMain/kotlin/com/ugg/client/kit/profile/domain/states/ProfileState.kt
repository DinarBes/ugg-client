package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Profile

data class ProfileState(
    val profile: Profile? = null,
    /** Nobody is signed in: the screen offers to sign in instead of the profile. */
    val isGuest: Boolean = false,
    val isLoading: Boolean = false,
    /** Pull-to-refresh in progress: the content stays on screen under the spinner. */
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)
