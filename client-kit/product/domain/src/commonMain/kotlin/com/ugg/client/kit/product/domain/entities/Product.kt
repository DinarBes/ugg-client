package com.ugg.client.kit.product.domain.entities

data class Product(
    val id: String = "",
    val kicker: String = "",
    val title: String = "",
    /** Already formatted: «14 900 ₽». */
    val price: String = "",
    /** Rubles — what the cart adds up. */
    val priceValue: Int = 0,
    /** «Сошьём за 5–7 дней». */
    val leadTime: String = "",
    val description: String = "",
    val photoCount: Int = 0,
    val shades: List<ProductShade> = emptyList(),
    val heights: List<ProductHeight> = emptyList(),
    val sizes: List<ProductSize> = emptyList(),
    /** Shown under the size grid, null when made-to-measure is not offered. */
    val customSizeHint: String? = null,
    val features: List<ProductFeature> = emptyList(),
    val details: List<ProductDetail> = emptyList(),
) {
    companion object {
        val mock get() = Product(
            id = "classic",
            kicker = "Ручная работа · Байкал",
            title = "Классика",
            price = "14 900 ₽",
            priceValue = 14900,
            leadTime = "Сошьём за 5–7 дней",
            description = "Высокие угги из цельной овчины с густым мехом внутри. Держат тепло в байкальские морозы и дышат в оттепель. Каждую пару шьёт один мастер — от раскроя до последнего стежка.",
            photoCount = 10,
            shades = listOf(
                ProductShade("chestnut", "Каштан", 0xFFB98A5E),
                ProductShade("sand", "Песок", 0xFFDCC6A6),
                ProductShade("milk", "Молоко", 0xFFF2EADF),
                ProductShade("chocolate", "Шоколад", 0xFF5A3E2E),
                ProductShade("graphite", "Графит", 0xFF5E5B57),
            ),
            heights = listOf(
                ProductHeight("standard", "Стандарт", "22 см"),
                ProductHeight("tall", "Высокие", "30 см"),
            ),
            sizes = (35..45).map { ProductSize(label = it.toString(), isAvailable = it != 44) },
            customSizeHint = "Нет вашего размера? Сошьём по мерке стопы — это бесплатно.",
            features = listOf(
                ProductFeature(ProductFeature.Kind.Material, "Натуральная овчина", "мех 18–22 мм внутри"),
                ProductFeature(ProductFeature.Kind.Handmade, "Ручной шов", "вощёная нить, двойная строчка"),
                ProductFeature(ProductFeature.Kind.Warmth, "Для сибирской зимы", "держат тепло до −30°"),
                ProductFeature(ProductFeature.Kind.Warranty, "Гарантия на шов", "12 месяцев с покупки"),
            ),
            details = listOf(
                ProductDetail(
                    "Материалы и состав",
                    "Верх и подкладка — натуральная овчина. Подошва — лёгкая EVA с протектором против скольжения. Стелька из шерстяного войлока, её можно вынуть и просушить.",
                ),
                ProductDetail(
                    "Уход",
                    "Сушите при комнатной температуре, вдали от батареи. Снег и соль снимайте мягкой щёткой, пятна — специальным средством для овчины.",
                ),
                ProductDetail(
                    "Доставка и возврат",
                    "Самовывоз из мастерской бесплатно, доставка СДЭК по России. Если пара не подошла по размеру — перешьём или вернём деньги в течение 14 дней.",
                ),
                ProductDetail(
                    "Примерка в наших точках",
                    "В Иркутске и Листвянке можно примерить угги всех цветов и снять мерку стопы у мастера.",
                ),
            ),
        )
    }
}
