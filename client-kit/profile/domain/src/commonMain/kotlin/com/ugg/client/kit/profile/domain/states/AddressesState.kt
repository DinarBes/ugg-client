package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Address

data class AddressesState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
