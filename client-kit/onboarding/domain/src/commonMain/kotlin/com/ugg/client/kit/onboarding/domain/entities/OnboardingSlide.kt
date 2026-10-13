package com.ugg.client.kit.onboarding.domain.entities

data class OnboardingSlide(
    val id: String = "",
    val overline: String = "",
    val title: String = "",
    val description: String = "",
) {
    companion object {
        val mock get() = listOf(
            OnboardingSlide(
                id = "workshop",
                overline = "Мастерская на Байкале",
                title = "Тепло, сшитое\nруками",
                description = "Одна модель, доведённая до совершенства: натуральная овчина, ручной шов и пять природных оттенков.",
            ),
            OnboardingSlide(
                id = "made-to-order",
                overline = "Под заказ",
                title = "Ваша пара —\nот мерки до шва",
                description = "Выберите цвет и размер, а мастер сошьёт пару за 5–7 дней. Нет вашего размера — сошьём по мерке стопы.",
            ),
            OnboardingSlide(
                id = "stores",
                overline = "Две точки на Байкале",
                title = "Приходите\nпримерить",
                description = "В Иркутске и Листвянке можно примерить угги, снять мерку и забрать готовый заказ.",
            ),
        )
    }
}
