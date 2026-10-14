package com.ugg.client.kit.profile.domain.states

data class NameState(
    val name: String = "",
    val isSaving: Boolean = false,
    val error: Throwable? = null,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && !isSaving
}
