package com.ugg.client.kit.home.domain.entities

data class CareTeaser(
    val kicker: String = "",
    val title: String = "",
    /** «3 минуты чтения». */
    val readTime: String = "",
)
