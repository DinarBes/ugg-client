package com.ugg.client.kit.auth.domain.states

data class RegistrationState(
    val name: String = "",
    val isSaving: Boolean = false,
    val error: Throwable? = null,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && !isSaving
}
