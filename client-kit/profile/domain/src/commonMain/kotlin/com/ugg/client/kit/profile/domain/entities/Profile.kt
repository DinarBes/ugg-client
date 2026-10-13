package com.ugg.client.kit.profile.domain.entities

data class Profile(
    val name: String = "",
    /** «АК» for the avatar. */
    val initials: String = "",
    /** Already formatted: «+7 900 000-00-00». */
    val phone: String = "",
    val activeOrder: ActiveOrder? = null,
    /** «38 · стандарт», `null` until the user has picked or measured a size. */
    val size: String? = null,
    /** «1 активный». */
    val ordersSummary: String = "",
    val menu: List<ProfileMenuItem> = emptyList(),
) {
    companion object {
        val mock get() = Profile(
            name = "Анна",
            initials = "АК",
            phone = "+7 900 000-00-00",
            activeOrder = ActiveOrder(
                number = "Заказ № 1024",
                details = "Каштан · 38",
                title = "Ваша пара у мастера",
                description = "Шьём вручную. Ориентировочно будет готова 18 октября — пришлём уведомление.",
                steps = listOf("Принят", "Раскрой", "Пошив", "Готов"),
                currentStep = 2,
                masterPhotoCount = 2,
            ),
            size = "38 · стандарт",
            ordersSummary = "1 активный",
            menu = listOf(
                ProfileMenuItem(ProfileMenuItem.Kind.Addresses, "Адреса доставки"),
                ProfileMenuItem(ProfileMenuItem.Kind.Care, "Уход за уггами"),
                ProfileMenuItem(ProfileMenuItem.Kind.Contact, "Написать в мастерскую"),
                ProfileMenuItem(ProfileMenuItem.Kind.Logout, "Выйти"),
            ),
        )
    }
}
