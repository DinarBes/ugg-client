package com.ugg.client.kit.core.data.formatting

import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import kotlinx.datetime.DayOfWeek

/**
 * The week in one line, consecutive days with the same hours joined:
 * «Ежедневно 10:00–20:00», «Пт–Вс 11:00–19:00», «Пн–Пт 10:00–20:00, Сб 11:00–18:00».
 * Days off are left out; a week with no working days is «Временно закрыто».
 */
fun StoreDto.ScheduleDto.summary(): String {
    val days = DayOfWeek.entries.map { it to hoursOn(it) }
    if (days.all { it.second == null }) return "Временно закрыто"

    val first = days.first().second
    if (first != null && days.all { it.second == first }) return "Ежедневно ${first.range()}"

    val groups = mutableListOf<Triple<DayOfWeek, DayOfWeek, StoreDto.HoursDto>>()
    for ((day, hours) in days) {
        if (hours == null) continue
        val last = groups.lastOrNull()
        if (last != null && last.third == hours && last.second.ordinal + 1 == day.ordinal) {
            groups[groups.lastIndex] = last.copy(second = day)
        } else {
            groups += Triple(day, day, hours)
        }
    }

    return groups.joinToString(", ") { (from, to, hours) ->
        val span = if (from == to) from.short() else "${from.short()}–${to.short()}"
        "$span ${hours.range()}"
    }
}

private fun StoreDto.HoursDto.range() = "$open–$close"

private fun DayOfWeek.short(): String = when (this) {
    DayOfWeek.MONDAY -> "Пн"
    DayOfWeek.TUESDAY -> "Вт"
    DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"
    DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"
    DayOfWeek.SUNDAY -> "Вс"
}
