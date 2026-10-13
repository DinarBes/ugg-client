package com.ugg.client.kit.stores.domain.states

import com.ugg.client.kit.stores.domain.entities.Store

data class StoresState(
    val stores: List<Store> = emptyList(),
    /** `null` — all cities. */
    val city: String? = null,
    /** Store highlighted by a tap on its map marker. */
    val selectedStoreId: String? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
) {
    val cities: List<String>
        get() = stores.map { it.city }.distinct()

    val visibleStores: List<Store>
        get() = if (city == null) stores else stores.filter { it.city == city }
}
