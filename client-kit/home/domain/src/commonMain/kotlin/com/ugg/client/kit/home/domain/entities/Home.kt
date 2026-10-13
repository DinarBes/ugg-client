package com.ugg.client.kit.home.domain.entities

/** Everything the home screen shows, section by section. */
data class Home(
    val hero: HomeHero = HomeHero(),
    val stats: List<HomeStat> = emptyList(),
    val shades: List<Shade> = emptyList(),
    val craftSteps: List<CraftStep> = emptyList(),
    val quote: MasterQuote? = null,
    val stores: List<StorePreview> = emptyList(),
    val care: CareTeaser? = null,
) {
    companion object {
        val mock get() = Home(
            hero = HomeHero(
                kicker = "Единственная модель",
                title = "Классика",
                description = "Овчина из хозяйств Прибайкалья, ручной шов и около шести часов работы мастера над каждой парой.",
                price = "14 900 ₽",
                leadTime = "шьём под заказ за 5–7 дней",
            ),
            stats = listOf(
                HomeStat("100%", "натуральная\nовчина"),
                HomeStat("6 ч", "ручной работы\nнад парой"),
                HomeStat("2", "точки\nна Байкале"),
            ),
            shades = listOf(
                Shade("chestnut", "Каштан", 0xFFB98A5E, isInStock = true),
                Shade("sand", "Песок", 0xFFD9C3A2, isInStock = true),
                Shade("milk", "Молоко", 0xFFEDE3D5, isInStock = false),
                Shade("chocolate", "Шоколад", 0xFF5A3E2E, isInStock = true),
                Shade("graphite", "Графит", 0xFF5E5B57, isInStock = false),
            ),
            craftSteps = listOf(
                CraftStep("01", "Выделка овчины", "Берём шкуры у хозяйств Прибайкалья и бережно выделываем мех."),
                CraftStep("02", "Раскрой", "Каждую деталь кроим вручную, подбирая мех по густоте."),
                CraftStep("03", "Ручной шов", "Прошиваем вощёной нитью — так шов служит годами."),
                CraftStep("04", "Проверка", "Каждую пару примеряем на колодку и проверяем шов."),
            ),
            quote = MasterQuote(
                text = "Каждую пару шьём так, будто для своей семьи — без спешки и без конвейера.",
                author = "Имя мастера",
                role = "основательница мастерской",
            ),
            stores = listOf(
                StorePreview("irkutsk", "Иркутск", "ул. Примерная, 12", "до 20:00", isOpen = true),
                StorePreview("listvyanka", "Листвянка", "ул. Береговая, 3", "откроется в пт", isOpen = false),
            ),
            care = CareTeaser(
                kicker = "Уход",
                title = "Как сохранить угги тёплыми на годы",
                readTime = "3 минуты чтения",
            ),
        )
    }
}
