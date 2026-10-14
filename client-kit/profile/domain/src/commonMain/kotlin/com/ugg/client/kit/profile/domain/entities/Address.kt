package com.ugg.client.kit.profile.domain.entities

data class Address(
    val id: String = "",
    val city: String = "",
    /** Street and house: «ул. Ленина, 12». */
    val street: String = "",
    /** Flat or office, optional. */
    val flat: String = "",
    /** For the courier: entrance, floor, intercom. */
    val comment: String = "",
    val isDefault: Boolean = false,
) {
    /** «Иркутск, ул. Ленина, 12, кв. 5». */
    val summary: String
        get() = listOf(city, street, flat.takeIf { it.isNotBlank() }?.let { "кв. $it" })
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
}
