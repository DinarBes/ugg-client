@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.core.data.formatting

import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Whether a store is open at a moment, worked out from its weekly schedule in its own time zone. */
sealed interface OpeningStatus {
    /** [closesAt]: «20:00». */
    data class Open(val closesAt: String) : OpeningStatus

    /**
     * [daysAhead]: 0 — opens later today, 1 — tomorrow, … `null` [opensAt] means the schedule
     * has no working day at all.
     */
    data class Closed(val opensAt: String?, val day: DayOfWeek?, val daysAhead: Int) : OpeningStatus

    val isOpen: Boolean
        get() = this is Open
}

private const val DEFAULT_TIMEZONE = "Asia/Irkutsk"

fun StoreDto.openingStatus(now: Instant): OpeningStatus {
    val zone = runCatching { TimeZone.of(timezone.ifEmpty { DEFAULT_TIMEZONE }) }.getOrDefault(TimeZone.of(DEFAULT_TIMEZONE))
    val local = now.toLocalDateTime(zone)
    val today = schedule.hoursOn(local.dayOfWeek)

    if (today != null) {
        val open = today.open.toLocalTimeOrNull()
        val close = today.close.toLocalTimeOrNull()
        if (open != null && close != null) {
            if (local.time >= open && local.time < close) return OpeningStatus.Open(today.close)
            if (local.time < open) return OpeningStatus.Closed(today.open, local.dayOfWeek, daysAhead = 0)
        }
    }

    for (daysAhead in 1..7) {
        val day = DayOfWeek.entries[(local.dayOfWeek.ordinal + daysAhead) % 7]
        val hours = schedule.hoursOn(day) ?: continue
        return OpeningStatus.Closed(hours.open, day, daysAhead)
    }
    return OpeningStatus.Closed(opensAt = null, day = null, daysAhead = 0)
}

/** The short badge on the home screen: «до 20:00», «откроется в 10:00», «откроется завтра», «откроется в пт». */
fun OpeningStatus.shortLabel(): String = when (this) {
    is OpeningStatus.Open -> "до $closesAt"
    is OpeningStatus.Closed -> when {
        opensAt == null || day == null -> "закрыто"
        daysAhead == 0 -> "откроется в $opensAt"
        daysAhead == 1 -> "откроется завтра"
        else -> "откроется ${day.inShort()}"
    }
}

/** The badge on the stores tab: «Открыто», «Откроется в 10:00», «Откроется завтра», «Откроется в пт». */
fun OpeningStatus.label(): String = when (this) {
    is OpeningStatus.Open -> "Открыто"
    is OpeningStatus.Closed -> shortLabel().replaceFirstChar { it.uppercaseChar() }
}

fun StoreDto.ScheduleDto.hoursOn(day: DayOfWeek): StoreDto.HoursDto? = when (day) {
    DayOfWeek.MONDAY -> mon
    DayOfWeek.TUESDAY -> tue
    DayOfWeek.WEDNESDAY -> wed
    DayOfWeek.THURSDAY -> thu
    DayOfWeek.FRIDAY -> fri
    DayOfWeek.SATURDAY -> sat
    DayOfWeek.SUNDAY -> sun
}

/** «в пт», «во вт». */
private fun DayOfWeek.inShort(): String = when (this) {
    DayOfWeek.MONDAY -> "в пн"
    DayOfWeek.TUESDAY -> "во вт"
    DayOfWeek.WEDNESDAY -> "в ср"
    DayOfWeek.THURSDAY -> "в чт"
    DayOfWeek.FRIDAY -> "в пт"
    DayOfWeek.SATURDAY -> "в сб"
    DayOfWeek.SUNDAY -> "в вс"
}

private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()
