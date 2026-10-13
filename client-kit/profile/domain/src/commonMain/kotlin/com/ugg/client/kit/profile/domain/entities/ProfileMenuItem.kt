package com.ugg.client.kit.profile.domain.entities

data class ProfileMenuItem(
    val kind: Kind = Kind.Addresses,
    val title: String = "",
) {
    /** What the row does; the View picks the icon from it. */
    enum class Kind { Addresses, Care, Contact, Logout }
}
