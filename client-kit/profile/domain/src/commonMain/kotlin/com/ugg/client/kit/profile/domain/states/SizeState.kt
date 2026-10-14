package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.SizeOptions

data class SizeState(
    val options: SizeOptions = SizeOptions(),
    val size: String? = null,
    val heightId: String? = null,
    /** As typed: «24,5». */
    val footLength: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: Throwable? = null,
) {
    /** The typed length, `null` when empty; [footLengthError] says when it is not a number. */
    val footLengthCm: Double?
        get() = footLength.replace(',', '.').trim().toDoubleOrNull()

    val footLengthError: String?
        get() = when {
            footLength.isBlank() -> null
            footLengthCm == null -> "Введите число, например 24,5"
            footLengthCm!! !in FOOT_LENGTH_RANGE -> "Обычно это от 20 до 32 см — проверьте замер"
            else -> null
        }

    val canSave: Boolean
        get() = !isLoading && !isSaving && footLengthError == null &&
            (size != null || footLengthCm != null)

    companion object {
        val FOOT_LENGTH_RANGE = 15.0..35.0
    }
}
