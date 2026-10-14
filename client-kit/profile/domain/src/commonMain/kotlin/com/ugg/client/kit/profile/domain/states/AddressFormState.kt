package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Address

data class AddressFormState(
    val address: Address = Address(),
    /** Editing a saved address rather than adding one: the form shows «Удалить». */
    val isExisting: Boolean = false,
    /** Set after the first «Сохранить», so empty fields are not red before the user tries. */
    val showErrors: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: Throwable? = null,
) {
    val cityError: String?
        get() = "Укажите город".takeIf { showErrors && address.city.isBlank() }

    val streetError: String?
        get() = "Укажите улицу и дом".takeIf { showErrors && address.street.isBlank() }

    val isValid: Boolean
        get() = address.city.isNotBlank() && address.street.isNotBlank()
}
