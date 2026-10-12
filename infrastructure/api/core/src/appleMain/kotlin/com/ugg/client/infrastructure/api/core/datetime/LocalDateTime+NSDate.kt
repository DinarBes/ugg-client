package com.ugg.client.infrastructure.api.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toNSDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDate {
    val instant = toInstant(timeZone)

    return instant.toNSDate()
}

@OptIn(ExperimentalTime::class)
fun LocalTime.toNSDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDate {
    val instance = Clock.System.now()
    val date = instance.toLocalDateTime(timeZone).date

    return LocalDateTime(date, this).toNSDate(timeZone)
}

fun LocalDate.toNSDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDate {
    val dateTime = LocalDateTime(this, LocalTime(0, 0))

    return dateTime.toNSDate(timeZone)
}

fun NSDate.toLocalDateTime(calendar: NSCalendar = NSCalendar.currentCalendar): LocalDateTime {
    val day = calendar.component(NSCalendarUnitDay, this).toInt()
    val month = calendar.component(NSCalendarUnitMonth, this).toInt()
    val year = calendar.component(NSCalendarUnitYear, this).toInt()
    val hours = calendar.component(NSCalendarUnitHour, this).toInt()
    val minutes = calendar.component(NSCalendarUnitMinute, this).toInt()
    val seconds = calendar.component(NSCalendarUnitSecond, this).toInt()

    return LocalDateTime(year, month, day, hours, minutes, seconds)
}
