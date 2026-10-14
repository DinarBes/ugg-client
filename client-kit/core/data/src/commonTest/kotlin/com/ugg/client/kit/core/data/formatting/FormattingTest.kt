@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.core.data.formatting

import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class FormattingTest {

    @Test
    fun rublesAreGroupedWithNonBreakingSpaces() {
        assertEquals("14 900 ₽", 14900.rubles())
        assertEquals("0 ₽", 0.rubles())
    }

    @Test
    fun hexColorBecomesOpaqueArgb() {
        assertEquals(0xFFB98A5E, "#B98A5E".toArgb())
        assertEquals(0xFFB98A5E, "not a color".toArgb())
    }

    @Test
    fun pluralFollowsRussianRules() {
        assertEquals(listOf("день", "дня", "дней", "дней", "день"), listOf(1, 3, 5, 11, 21).map { it.plural("день", "дня", "дней") })
    }

    // Irkutsk is UTC+8: 04:00Z is 12:00 local. 2026-10-14 is a Wednesday.
    private val wednesdayNoon = Instant.parse("2026-10-14T04:00:00Z")
    private val wednesdayMorning = Instant.parse("2026-10-14T00:30:00Z") // 08:30
    private val wednesdayNight = Instant.parse("2026-10-14T13:00:00Z")   // 21:00

    private val daily = store(StoreDto.ScheduleDto(
        mon = HOURS, tue = HOURS, wed = HOURS, thu = HOURS, fri = HOURS, sat = HOURS, sun = HOURS,
    ))
    private val weekendsOnly = store(StoreDto.ScheduleDto(fri = HOURS, sat = HOURS, sun = HOURS))

    @Test
    fun openDuringWorkingHours() {
        assertEquals(OpeningStatus.Open("20:00"), daily.openingStatus(wednesdayNoon))
        assertEquals("до 20:00", daily.openingStatus(wednesdayNoon).shortLabel())
        assertEquals("Открыто", daily.openingStatus(wednesdayNoon).label())
    }

    @Test
    fun beforeOpeningItOpensToday() {
        assertEquals("откроется в 10:00", daily.openingStatus(wednesdayMorning).shortLabel())
    }

    @Test
    fun afterClosingItOpensTomorrow() {
        assertEquals("Откроется завтра", daily.openingStatus(wednesdayNight).label())
    }

    @Test
    fun closedOnWeekdaysOpensOnFriday() {
        val status = weekendsOnly.openingStatus(wednesdayNoon)
        assertEquals(false, status.isOpen)
        assertEquals("откроется в пт", status.shortLabel())
    }

    @Test
    fun noWorkingDaysAtAll() {
        assertEquals("закрыто", store(StoreDto.ScheduleDto()).openingStatus(wednesdayNoon).shortLabel())
    }

    private fun store(schedule: StoreDto.ScheduleDto) = StoreDto(timezone = "Asia/Irkutsk", schedule = schedule)

    private companion object {
        val HOURS = StoreDto.HoursDto("10:00", "20:00")
    }
}

class PhoneAndDateTest {
    @Test
    fun russianPhoneIsGrouped() {
        assertEquals("+7 900 123-45-67", "+79001234567".formatPhone())
        assertEquals("+7 900 123-45-67", "89001234567".formatPhone())
        assertEquals("+1 650 555", "+1 650 555".formatPhone())
    }

    @Test
    fun dateIsDayAndGenitiveMonth() {
        // 2026-10-18 00:30 in Irkutsk.
        assertEquals("18 октября", Instant.parse("2026-10-17T16:30:00Z").toEpochMilliseconds().toDayAndMonth())
    }
}
