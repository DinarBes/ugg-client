package com.ugg.client.kit.profile.data.mappers

import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.kit.profile.domain.entities.Address

internal fun AddressDto.toDomain() = Address(
    id = id,
    city = city,
    street = street,
    flat = flat,
    comment = comment,
    isDefault = isDefault,
)

internal fun Address.toDto() = AddressDto(
    id = id,
    city = city.trim(),
    street = street.trim(),
    flat = flat.trim(),
    comment = comment.trim(),
    isDefault = isDefault,
)

/** The default first, then by city and street — the order the list and checkout show. */
internal fun List<Address>.sortedForDisplay() =
    sortedWith(compareByDescending<Address> { it.isDefault }.thenBy { it.city }.thenBy { it.street })
