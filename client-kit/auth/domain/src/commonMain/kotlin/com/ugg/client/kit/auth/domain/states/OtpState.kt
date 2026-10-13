package com.ugg.client.kit.auth.domain.states

data class OtpState(
    val otp: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    /** Session of the last resent code; `null` — still the one from the phone step. */
    val sessionId: String? = null,
    /** Bumped on every resend so the screen restarts its countdown. */
    val resendCount: Int = 0,
    val error: Throwable? = null,
)
