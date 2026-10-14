package com.ugg.client.kit.care.data.mappers

import com.ugg.client.infrastructure.firebase.firestore.dto.CareContentDto
import com.ugg.client.kit.care.domain.entities.Care
import com.ugg.client.kit.care.domain.entities.CareContact
import com.ugg.client.kit.care.domain.entities.CareTip
import com.ugg.client.kit.core.data.formatting.plural

internal fun CareContentDto.toDomain() = Care(
    kicker = kicker,
    title = title,
    intro = intro,
    readTime = readMinutes.takeIf { it > 0 }?.let { "$it ${it.plural("минута", "минуты", "минут")} чтения" }.orEmpty(),
    tips = tips.map { CareTip(kind = it.kind.toTipKind(), title = it.title, body = it.body) },
    dontsTitle = dontsTitle.ifBlank { "Чего не делать" },
    donts = donts.filter { it.isNotBlank() },
    // An empty card in the console means «don't show it».
    contact = contact
        ?.takeIf { it.title.isNotBlank() || it.body.isNotBlank() }
        ?.let { CareContact(title = it.title, body = it.body, button = it.button.ifBlank { "Написать в мастерскую" }) },
)

/** Unknown kinds get a neutral icon rather than breaking the screen. */
private fun String.toTipKind() = when (this) {
    "snow" -> CareTip.Kind.Snow
    "drying" -> CareTip.Kind.Drying
    "salt" -> CareTip.Kind.Salt
    "cleaning" -> CareTip.Kind.Cleaning
    "protection" -> CareTip.Kind.Protection
    "storage" -> CareTip.Kind.Storage
    else -> CareTip.Kind.Other
}
