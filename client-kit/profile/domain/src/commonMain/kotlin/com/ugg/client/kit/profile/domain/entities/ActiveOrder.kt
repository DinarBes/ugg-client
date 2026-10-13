package com.ugg.client.kit.profile.domain.entities

/** The order being sewn right now, shown as the dark card on the profile. */
data class ActiveOrder(
    /** «Заказ № 1024». */
    val number: String = "",
    /** «Каштан · 38». */
    val details: String = "",
    val title: String = "",
    val description: String = "",
    /** «Принят», «Раскрой», «Пошив», «Готов». */
    val steps: List<String> = emptyList(),
    /** Index in [steps] of the stage in progress; earlier ones are done. */
    val currentStep: Int = 0,
    /** How many process photos the master has sent; `0` hides the block. */
    val masterPhotoCount: Int = 0,
)
