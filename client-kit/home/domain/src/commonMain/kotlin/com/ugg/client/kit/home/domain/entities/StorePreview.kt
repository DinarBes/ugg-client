package com.ugg.client.kit.home.domain.entities

/** Short store card on the home screen; the full one lives on the «Точки» tab. */
data class StorePreview(
    val id: String = "",
    val city: String = "",
    val address: String = "",
    /** Already phrased for the badge: «до 20:00», «откроется в пт». */
    val hours: String = "",
    val isOpen: Boolean = false,
)
