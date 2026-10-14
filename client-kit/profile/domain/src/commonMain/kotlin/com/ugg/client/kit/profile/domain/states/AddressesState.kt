package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Address

data class AddressesState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    /** Pull-to-refresh in progress: the content stays on screen under the spinner. */
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)
