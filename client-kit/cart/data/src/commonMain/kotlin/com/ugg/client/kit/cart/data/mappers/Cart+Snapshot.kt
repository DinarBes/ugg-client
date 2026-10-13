package com.ugg.client.kit.cart.data.mappers

import com.ugg.client.kit.cart.data.storage.CartSnapshot
import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.cart.domain.entities.CartExtra
import com.ugg.client.kit.cart.domain.entities.CartLine
import com.ugg.client.kit.cart.domain.entities.CartSummaryRow
import com.ugg.client.kit.cart.domain.entities.DeliveryOption

/** Accessory sold next to the boots. Prices in rubles. */
internal data class ExtraOffer(val id: String, val title: String, val details: String, val price: Int)

/** [price] is the minimum when [isFromPrice]: СДЭК depends on the city. */
internal data class DeliveryOffer(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val isFromPrice: Boolean = false,
)

internal const val GIFT_WRAP_PRICE = 0

fun CartSnapshot.toCart(): Cart {
    val delivery = CartCatalog.deliveries.firstOrNull { it.id == deliveryId } ?: CartCatalog.deliveries.first()
    val itemsTotal = lines.sumOf { it.unitPrice * it.quantity }
    val itemCount = lines.sumOf { it.quantity }
    val total = itemsTotal + delivery.price + if (isGiftWrapped) GIFT_WRAP_PRICE else 0

    return Cart(
        lines = lines.map {
            CartLine(
                id = it.id,
                title = it.title,
                details = it.details,
                price = (it.unitPrice * it.quantity).rubles(),
                quantity = it.quantity,
                isExtra = it.isExtra,
            )
        },
        extras = CartCatalog.extras
            .filter { offer -> lines.none { it.id == offer.id } }
            .map { CartExtra(id = it.id, title = it.title, price = it.price.rubles()) },
        deliveryOptions = CartCatalog.deliveries.map { it.toDeliveryOption() },
        selectedDeliveryId = delivery.id,
        isGiftWrapped = isGiftWrapped,
        giftWrapTitle = "Подарочная упаковка",
        giftWrapDescription = "льняной мешок и открытка от мастера",
        summary = listOfNotNull(
            CartSummaryRow("Товары", itemsTotal.rubles()),
            CartSummaryRow("Подарочная упаковка", GIFT_WRAP_PRICE.rubles()).takeIf { isGiftWrapped },
            CartSummaryRow("Получение", delivery.priceLabel()),
        ),
        total = total.rubles(),
        subtitle = "$itemCount ${itemCount.pluralGoods()} · ${total.rubles()}",
        itemCount = itemCount,
    )
}

private fun DeliveryOffer.toDeliveryOption() = DeliveryOption(
    id = id,
    title = title,
    description = description,
    price = priceLabel(),
)

private fun DeliveryOffer.priceLabel(): String = when {
    price == 0 -> "Бесплатно"
    isFromPrice -> "от ${price.rubles()}"
    else -> price.rubles()
}

/** 14900 → «14 900 ₽», with non-breaking spaces so the price never wraps. */
internal fun Int.rubles(): String {
    val grouped = toString().reversed().chunked(3).joinToString(" ").reversed()
    return "$grouped ₽"
}

private fun Int.pluralGoods(): String {
    val lastTwo = this % 100
    val last = this % 10

    return when {
        lastTwo in 11..14 -> "товаров"
        last == 1 -> "товар"
        last in 2..4 -> "товара"
        else -> "товаров"
    }
}

/** What the shop sells besides the boots and how it delivers. Moves to Firebase with the products. */
internal object CartCatalog {
    val extras = listOf(
        ExtraOffer("care-cream", "Крем для овчины", "уход", 890),
        ExtraOffer("felt-insoles", "Стельки из войлока", "пара", 690),
        ExtraOffer("linen-bag", "Льняной мешок", "для хранения", 490),
    )

    val deliveries = listOf(
        DeliveryOffer(
            id = CartSnapshot.DEFAULT_DELIVERY_ID,
            title = "Самовывоз из мастерской",
            description = "Иркутск, ул. Примерная, 12 · когда пара будет готова",
            price = 0,
        ),
        DeliveryOffer("cdek", "Доставка СДЭК", "3–5 дней после пошива", 450, isFromPrice = true),
        DeliveryOffer("courier", "Курьер по Иркутску", "в удобный вам день", 350),
    )
}
