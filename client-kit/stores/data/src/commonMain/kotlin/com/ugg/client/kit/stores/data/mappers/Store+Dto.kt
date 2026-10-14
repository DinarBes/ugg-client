@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.stores.data.mappers

import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.kit.core.data.formatting.label
import com.ugg.client.kit.core.data.formatting.openingStatus
import com.ugg.client.kit.core.data.formatting.summary
import com.ugg.client.kit.stores.domain.entities.Store
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal fun StoreDto.toDomain(now: Instant): Store {
    val status = openingStatus(now)
    val latitude = location.latitude
    val longitude = location.longitude

    return Store(
        id = id,
        city = city,
        name = name,
        address = address,
        hours = schedule.summary(),
        status = status.label(),
        isOpen = status.isOpen,
        services = services,
        latitude = latitude,
        longitude = longitude,
        phoneUri = phone.filter { it.isDigit() || it == '+' }.takeIf { it.isNotEmpty() }?.let { "tel:$it" }.orEmpty(),
        // Opens Yandex Maps with a route to the store, or the website when the app is missing.
        routeUri = "https://yandex.ru/maps/?rtext=~$latitude,$longitude&rtt=auto",
    )
}
