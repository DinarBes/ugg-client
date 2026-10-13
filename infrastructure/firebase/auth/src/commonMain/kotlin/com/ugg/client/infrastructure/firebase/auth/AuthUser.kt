package com.ugg.client.infrastructure.firebase.auth

/** Signed-in Firebase user, without any Firebase types. */
data class AuthUser(
    val uid: String,
    /** E.164, «+79001234567»; `null` only for non-phone sign-ins. */
    val phone: String?,
)
