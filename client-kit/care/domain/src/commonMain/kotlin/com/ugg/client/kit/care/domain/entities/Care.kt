package com.ugg.client.kit.care.domain.entities

data class Care(
    val kicker: String = "",
    /** May contain `\n`. */
    val title: String = "",
    val intro: String = "",
    /** «3 минуты чтения». */
    val readTime: String = "",
    val tips: List<CareTip> = emptyList(),
    val dontsTitle: String = "",
    val donts: List<String> = emptyList(),
    /** The dark card at the bottom; `null` hides it. */
    val contact: CareContact? = null,
) {
    companion object {
        val mock get() = Care(
            kicker = "Уход",
            title = "Как сохранить угги\nтёплыми на годы",
            intro = "Овчина — живой материал: она греет, дышит и служит много зим, если о ней немного позаботиться.",
            readTime = "3 минуты чтения",
            tips = listOf(
                CareTip(CareTip.Kind.Snow, "После снега", "Стряхните снег, как только зашли в дом, и промокните влажные места полотенцем."),
                CareTip(CareTip.Kind.Drying, "Сушка", "Сушите при комнатной температуре, набив носок бумагой, — не у батареи."),
                CareTip(CareTip.Kind.Storage, "Хранение летом", "Почистите, просушите и уберите в льняной мешок."),
            ),
            dontsTitle = "Чего не делать",
            donts = listOf("Стирать в машине и замачивать", "Сушить у батареи, феном или на солнце"),
            contact = CareContact(
                title = "Разошёлся шов или нужен совет?",
                body = "Напишите мастеру — подскажем по уходу, а шов в течение 12 месяцев починим бесплатно.",
                button = "Написать в мастерскую",
            ),
        )
    }
}

/** One piece of advice; the View picks the icon from [kind]. */
data class CareTip(
    val kind: Kind = Kind.Other,
    val title: String = "",
    val body: String = "",
) {
    enum class Kind { Snow, Drying, Salt, Cleaning, Protection, Storage, Other }
}

data class CareContact(
    val title: String = "",
    val body: String = "",
    val button: String = "",
)
