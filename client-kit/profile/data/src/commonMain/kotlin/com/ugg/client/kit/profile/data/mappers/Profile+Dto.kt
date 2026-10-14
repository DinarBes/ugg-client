package com.ugg.client.kit.profile.data.mappers

import com.ugg.client.infrastructure.firebase.auth.AuthUser
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderItemDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderStatus
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.kit.core.data.formatting.formatPhone
import com.ugg.client.kit.core.data.formatting.plural
import com.ugg.client.kit.core.data.formatting.toDayAndMonth
import com.ugg.client.kit.profile.domain.entities.ActiveOrder
import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.entities.ProfileMenuItem

/** Stages on the order card, in order; the index of the current one comes from the status. */
internal val ORDER_STEPS = listOf("Принят", "Раскрой", "Пошив", "Готов")

/**
 * The profile of a signed-in user. [user] is `null` until the profile document is first saved,
 * so a fresh account still gets a profile, with the phone from Firebase Auth.
 */
internal fun profileFrom(auth: AuthUser, user: UserDto?, orders: List<OrderDto>): Profile {
    val name = user?.name?.trim().orEmpty()
    val phone = auth.phone ?: user?.phone.orEmpty()
    val active = orders.filter { it.status.isActive }

    return Profile(
        name = name.ifEmpty { "Покупатель" },
        initials = name.initials(),
        phone = phone.formatPhone(),
        activeOrder = active.maxByOrNull { it.createdAt ?: 0 }?.toActiveOrder(),
        size = user?.size?.toSummary(),
        ordersSummary = ordersSummary(activeCount = active.size, total = orders.size),
        menu = SIGNED_IN_MENU,
    )
}

/** «1 активный», «2 активных», «3 заказа», «пока нет». */
internal fun ordersSummary(activeCount: Int, total: Int) = when {
    activeCount > 0 -> "$activeCount ${activeCount.plural("активный", "активных", "активных")}"
    total > 0 -> "$total ${total.plural("заказ", "заказа", "заказов")}"
    else -> "пока нет"
}

internal fun OrderDto.toActiveOrder(): ActiveOrder {
    val boots = items.firstOrNull { it.kind == OrderItemDto.KIND_BOOTS }

    return ActiveOrder(
        number = "Заказ № $number",
        details = listOfNotNull(boots?.shadeName, boots?.size).joinToString(" · "),
        title = when (status) {
            OrderStatus.Accepted -> "Заказ принят"
            OrderStatus.Ready -> "Ваша пара готова"
            else -> "Ваша пара у мастера"
        },
        description = description(),
        steps = ORDER_STEPS,
        currentStep = when (status) {
            OrderStatus.Accepted -> 0
            OrderStatus.Cutting -> 1
            OrderStatus.Sewing -> 2
            else -> 3
        },
        masterPhotoCount = masterPhotos.size,
    )
}

private fun OrderDto.description(): String = when (status) {
    OrderStatus.Accepted -> "Мастер свяжется с вами, чтобы уточнить детали, и возьмётся за раскрой."
    OrderStatus.Ready -> if (delivery.storeId != null) "Можно забирать — ждём вас в мастерской." else "Передаём пару в доставку."
    else -> estimatedReadyAt
        ?.let { "Шьём вручную. Ориентировочно будет готова ${it.toDayAndMonth()} — пришлём уведомление." }
        ?: "Шьём вручную. Пришлём уведомление, когда пара будет готова."
}

/** «Анна Королёва» → «АК», «Анна» → «А». */
private fun String.initials() = split(' ')
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }

/** «38 · стандарт», or «стопа 24,5 см» when only the foot was measured; `null` when nothing is set. */
internal fun UserDto.UserSizeDto.toSummary(): String? {
    val parts = listOfNotNull(label.takeIf { it.isNotBlank() }, heightId.heightName())
    if (parts.isNotEmpty()) return parts.joinToString(" · ")
    return footLengthCm?.let { "стопа ${it.toCentimeters()} см" }
}

/** 24.5 → «24,5», 25.0 → «25». */
internal fun Double.toCentimeters(): String {
    val tenths = kotlin.math.round(this * 10).toLong()
    return if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10},${tenths % 10}"
}

private fun String.heightName(): String? = when (this) {
    "" -> null
    "standard" -> "стандарт"
    "tall" -> "высокие"
    else -> this
}

internal val SIGNED_IN_MENU = listOf(
    ProfileMenuItem(ProfileMenuItem.Kind.Addresses, "Адреса доставки"),
    ProfileMenuItem(ProfileMenuItem.Kind.Care, "Уход за уггами"),
    ProfileMenuItem(ProfileMenuItem.Kind.Contact, "Написать в мастерскую"),
    ProfileMenuItem(ProfileMenuItem.Kind.Logout, "Выйти"),
)
