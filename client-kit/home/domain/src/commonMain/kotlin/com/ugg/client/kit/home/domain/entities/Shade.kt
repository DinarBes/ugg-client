package com.ugg.client.kit.home.domain.entities

/** One of the sheepskin colors the model is sewn in. */
data class Shade(
    val id: String = "",
    val name: String = "",
    /** ARGB, e.g. `0xFFB98A5E` — the swatch and the photo-placeholder tint. */
    val color: Long = 0xFFB98A5E,
    val isInStock: Boolean = true,
)
