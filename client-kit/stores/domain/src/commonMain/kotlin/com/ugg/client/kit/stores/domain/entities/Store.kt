package com.ugg.client.kit.stores.domain.entities

data class Store(
    val id: String = "",
    val city: String = "",
    val name: String = "",
    val address: String = "",
    /** «Ежедневно 10:00–20:00». */
    val hours: String = "",
    /** Already phrased for the badge: «Открыто», «Откроется в пт». */
    val status: String = "",
    val isOpen: Boolean = false,
    val services: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    /** `tel:` link for «Позвонить». */
    val phoneUri: String = "",
    /** Route link for «Маршрут»: opens Yandex Maps, or the browser when the app is missing. */
    val routeUri: String = "",
) {
    companion object {
        val mock get() = listOf(
            Store(
                id = "irkutsk",
                city = "Иркутск",
                name = "Мастерская и магазин",
                address = "ул. Примерная, 12 · 2 этаж",
                hours = "Ежедневно 10:00–20:00",
                status = "Открыто",
                isOpen = true,
                services = listOf("Примерка", "Мерка стопы", "Самовывоз"),
                latitude = 52.2870,
                longitude = 104.2810,
                phoneUri = "tel:+79000000000",
                routeUri = "https://yandex.ru/maps/?rtext=~52.2870,104.2810&rtt=auto",
            ),
            Store(
                id = "listvyanka",
                city = "Листвянка",
                name = "Шоурум у набережной",
                address = "ул. Береговая, 3",
                hours = "Пт–Вс 11:00–19:00",
                status = "Откроется в пт",
                isOpen = false,
                services = listOf("Примерка", "Мерка стопы", "Самовывоз"),
                latitude = 51.8530,
                longitude = 104.8700,
                phoneUri = "tel:+79000000001",
                routeUri = "https://yandex.ru/maps/?rtext=~51.8530,104.8700&rtt=auto",
            ),
        )
    }
}
