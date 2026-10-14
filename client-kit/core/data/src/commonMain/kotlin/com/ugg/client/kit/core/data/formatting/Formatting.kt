package com.ugg.client.kit.core.data.formatting

/** 14900 → «14 900 ₽», with non-breaking spaces so the price never wraps. */
fun Int.rubles(): String {
    val grouped = toString().reversed().chunked(3).joinToString(" ").reversed()
    return "$grouped ₽"
}

/** `"#B98A5E"` → `0xFFB98A5E`; anything unreadable falls back to the chestnut sheepskin. */
fun String.toArgb(): Long {
    val hex = removePrefix("#")
    val rgb = hex.takeIf { it.length == 6 }?.toLongOrNull(16) ?: return FALLBACK_COLOR
    return 0xFF000000 or rgb
}

private const val FALLBACK_COLOR = 0xFFB98A5E

/** Russian plural: `5.plural("день", "дня", "дней")` → «дней». */
fun Int.plural(one: String, few: String, many: String): String {
    val lastTwo = this % 100
    val last = this % 10
    return when {
        lastTwo in 11..14 -> many
        last == 1 -> one
        last in 2..4 -> few
        else -> many
    }
}

/** `+79001234567` → «+7 900 123-45-67»; other formats are shown as they are. */
fun String.formatPhone(): String {
    val digits = filter { it.isDigit() }
    if (digits.length != 11 || !(digits.startsWith("7") || digits.startsWith("8"))) return this
    val n = digits.drop(1)
    return "+7 ${n.substring(0, 3)} ${n.substring(3, 6)}-${n.substring(6, 8)}-${n.substring(8, 10)}"
}
