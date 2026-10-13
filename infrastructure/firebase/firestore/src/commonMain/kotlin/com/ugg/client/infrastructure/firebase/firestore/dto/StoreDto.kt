package com.ugg.client.infrastructure.firebase.firestore.dto

import kotlinx.serialization.Serializable

/**
 * `stores/{id}`. Opening hours are stored per weekday in the store's own [timezone]; «Открыто» and
 * «Откроется в пт» are worked out by the app, so they never go stale.
 */
@Serializable
data class StoreDto(
    val id: String = "",
    val order: Int = 0,
    val city: String = "",
    val name: String = "",
    val address: String = "",
    /** E.164: `+79000000000`. */
    val phone: String = "",
    val location: GeoPointDto = GeoPointDto(),
    val services: List<String> = emptyList(),
    /** IANA id: `Asia/Irkutsk`. */
    val timezone: String = "",
    val schedule: ScheduleDto = ScheduleDto(),
) {
    /** `null` for a day the store is closed. */
    @Serializable
    data class ScheduleDto(
        val mon: HoursDto? = null,
        val tue: HoursDto? = null,
        val wed: HoursDto? = null,
        val thu: HoursDto? = null,
        val fri: HoursDto? = null,
        val sat: HoursDto? = null,
        val sun: HoursDto? = null,
    )

    /** `"10:00"`–`"20:00"`, local time of the store. */
    @Serializable
    data class HoursDto(val open: String = "", val close: String = "")
}

@Serializable
data class GeoPointDto(val latitude: Double = 0.0, val longitude: Double = 0.0)
