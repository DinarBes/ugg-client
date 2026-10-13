package com.ugg.client.kit.home.domain.entities

data class HomeStat(
    /** «100%», «6 ч». */
    val value: String = "",
    /** May contain `\n`. */
    val caption: String = "",
)
