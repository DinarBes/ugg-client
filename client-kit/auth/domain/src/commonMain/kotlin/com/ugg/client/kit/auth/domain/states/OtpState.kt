package com.ugg.client.kit.auth.domain.states

data class OtpState(
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
