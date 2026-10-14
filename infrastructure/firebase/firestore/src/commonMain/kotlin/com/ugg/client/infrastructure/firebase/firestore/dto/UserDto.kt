package com.ugg.client.infrastructure.firebase.firestore.dto

import kotlinx.serialization.Serializable

/** `users/{uid}`; [id] is the Firebase Auth uid. */
@Serializable
data class UserDto(
    val id: String = "",
    val name: String = "",
    /** E.164, copied from Firebase Auth so the workshop sees it next to the orders. */
    val phone: String = "",
    /** `null` until the user has picked or measured a size. */
    val size: UserSizeDto? = null,
    /** Epoch milliseconds, set by the server on the first save. */
    val createdAt: Long? = null,
) {
    /**
     * [label]: «38»; [heightId]: `standard` / `tall`; [footLengthCm]: the measured foot,
     * so the master can sew to measure when the size grid does not fit.
     */
    @Serializable
    data class UserSizeDto(val label: String = "", val heightId: String = "", val footLengthCm: Double? = null)
}

/** `users/{uid}/addresses/{id}`. */
@Serializable
data class AddressDto(
    val id: String = "",
    val city: String = "",
    val street: String = "",
    val flat: String = "",
    val comment: String = "",
    val isDefault: Boolean = false,
)
