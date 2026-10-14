package com.ugg.client.kit.home.data.mappers

import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StepDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.kit.core.data.formatting.openingStatus
import com.ugg.client.kit.core.data.formatting.plural
import com.ugg.client.kit.core.data.formatting.rubles
import com.ugg.client.kit.core.data.formatting.shortLabel
import com.ugg.client.kit.core.data.formatting.toArgb
import com.ugg.client.kit.home.domain.entities.CareTeaser
import com.ugg.client.kit.home.domain.entities.CraftStep
import com.ugg.client.kit.home.domain.entities.Home
import com.ugg.client.kit.home.domain.entities.HomeHero
import com.ugg.client.kit.home.domain.entities.HomeStat
import com.ugg.client.kit.home.domain.entities.MasterQuote
import com.ugg.client.kit.home.domain.entities.Shade
import com.ugg.client.kit.home.domain.entities.StorePreview
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The home screen from three documents: its texts, the product and the stores. */
internal fun homeFrom(content: HomeContentDto, product: ProductDto, stores: List<StoreDto>, now: Instant) = Home(
    hero = HomeHero(
        kicker = content.heroKicker,
        title = product.title,
        description = content.heroDescription,
        price = product.price.rubles(),
        leadTime = product.leadTimeDays.toLeadTime(),
    ),
    stats = content.stats.map { HomeStat(value = it.value, caption = it.caption) },
    // The same shades the «Модель» tab offers: the ones switched off in the admin app are left out.
    shades = product.shades.filter { it.inStock }.map { Shade(id = it.id, name = it.name, color = it.color.toArgb(), isInStock = it.inStock) },
    craftSteps = content.craftSteps.map { it.toCraftStep() },
    quote = content.quote?.let { MasterQuote(text = it.text, author = it.author, role = it.role) },
    stores = stores.map { it.toPreview(now) },
    care = content.care?.let {
        CareTeaser(kicker = it.kicker, title = it.title, readTime = "${it.readMinutes} ${it.readMinutes.plural("минута", "минуты", "минут")} чтения")
    },
)

/** {5, 7} → «шьём под заказ за 5–7 дней». */
private fun ProductDto.LeadTimeDto.toLeadTime(): String {
    if (max <= 0) return ""
    val days = if (min in 1 until max) "$min–$max" else "$max"
    return "шьём под заказ за $days ${max.plural("день", "дня", "дней")}"
}

private fun StepDto.toCraftStep() = CraftStep(number = number, title = title, description = description)

private fun StoreDto.toPreview(now: Instant): StorePreview {
    val status = openingStatus(now)
    return StorePreview(id = id, city = city, address = address, hours = status.shortLabel(), isOpen = status.isOpen)
}
