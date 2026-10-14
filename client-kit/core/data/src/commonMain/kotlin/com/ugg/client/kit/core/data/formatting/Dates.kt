@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.core.data.formatting

import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Epoch milliseconds → «18 октября», in the workshop's time zone. */
fun Long.toDayAndMonth(timezone: String = "Asia/Irkutsk"): String {
    val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.of(timezone)).date
    return "${date.day} ${date.month.genitive()}"
}

private fun Month.genitive() = when (this) {
    Month.JANUARY -> "января"
    Month.FEBRUARY -> "февраля"
    Month.MARCH -> "марта"
    Month.APRIL -> "апреля"
    Month.MAY -> "мая"
    Month.JUNE -> "июня"
    Month.JULY -> "июля"
    Month.AUGUST -> "августа"
    Month.SEPTEMBER -> "сентября"
    Month.OCTOBER -> "октября"
    Month.NOVEMBER -> "ноября"
    Month.DECEMBER -> "декабря"
}
