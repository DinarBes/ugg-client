package com.ugg.client.kit.core.data.formatting

import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleSummaryTest {
    private val day = StoreDto.HoursDto("10:00", "20:00")
    private val short = StoreDto.HoursDto("11:00", "18:00")

    @Test
    fun sameHoursEveryDayIsDaily() {
        val schedule = StoreDto.ScheduleDto(mon = day, tue = day, wed = day, thu = day, fri = day, sat = day, sun = day)
        assertEquals("Ежедневно 10:00–20:00", schedule.summary())
    }

    @Test
    fun weekendOnly() {
        val hours = StoreDto.HoursDto("11:00", "19:00")
        assertEquals("Пт–Вс 11:00–19:00", StoreDto.ScheduleDto(fri = hours, sat = hours, sun = hours).summary())
    }

    @Test
    fun differentHoursMakeSeparateGroups() {
        val schedule = StoreDto.ScheduleDto(mon = day, tue = day, wed = day, thu = day, fri = day, sat = short)
        assertEquals("Пн–Пт 10:00–20:00, Сб 11:00–18:00", schedule.summary())
    }

    @Test
    fun aDayOffBreaksTheGroup() {
        val schedule = StoreDto.ScheduleDto(mon = day, wed = day)
        assertEquals("Пн 10:00–20:00, Ср 10:00–20:00", schedule.summary())
    }

    @Test
    fun noWorkingDays() {
        assertEquals("Временно закрыто", StoreDto.ScheduleDto().summary())
    }
}
