package com.ugg.client.kit.profile.domain.entities

/** Who is signed in, as the settings show it. */
data class Account(
    /** Empty until the user gives one. */
    val name: String = "",
    /** «+7 900 123-45-67». */
    val phone: String = "",
)
