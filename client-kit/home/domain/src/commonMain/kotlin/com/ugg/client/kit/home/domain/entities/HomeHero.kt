package com.ugg.client.kit.home.domain.entities

/** The single model, shown big at the top of the home screen. */
data class HomeHero(
    val kicker: String = "",
    val title: String = "",
    val description: String = "",
    /** Already formatted: «14 900 ₽». */
    val price: String = "",
    val leadTime: String = "",
)
