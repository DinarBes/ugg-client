@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.stores.data

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.CareContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.GeoPointDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import com.ugg.client.kit.stores.data.useCases.SharedStoresUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SharedStoresUseCaseTest {
    // 12:00 in Irkutsk on a Wednesday.
    private val noon = Instant.parse("2026-10-14T04:00:00Z")

    @Test
    fun storesAreFormattedForTheScreen() = runTest {
        val stores = SharedStoresUseCase(FakeCatalog(), now = { noon }).fetch()

        val irkutsk = stores[0]
        assertEquals("Ежедневно 10:00–20:00", irkutsk.hours)
        assertEquals("Открыто", irkutsk.status)
        assertEquals(true, irkutsk.isOpen)
        assertEquals("tel:+79000000000", irkutsk.phoneUri)
        assertEquals("https://yandex.ru/maps/?rtext=~52.287,104.281&rtt=auto", irkutsk.routeUri)

        val listvyanka = stores[1]
        assertEquals("Пт–Вс 11:00–19:00", listvyanka.hours)
        assertEquals("Откроется в пт", listvyanka.status)
        assertEquals(false, listvyanka.isOpen)
    }

    @Test
    fun noNetworkBecomesNoConnection() = runTest {
        val catalog = FakeCatalog(failure = FirestoreException(FirestoreException.Reason.Unavailable))

        assertFailsWith<NoConnectionException> { SharedStoresUseCase(catalog, now = { noon }).fetch() }
    }

    private class FakeCatalog(private val failure: Exception? = null) : CatalogService {
        private val daily = StoreDto.HoursDto("10:00", "20:00")
        private val weekend = StoreDto.HoursDto("11:00", "19:00")

        override suspend fun stores() = failure?.let { throw it } ?: listOf(
            StoreDto(
                id = "irkutsk", city = "Иркутск", phone = "+7 900 000-00-00", timezone = "Asia/Irkutsk",
                location = GeoPointDto(52.287, 104.281),
                schedule = StoreDto.ScheduleDto(mon = daily, tue = daily, wed = daily, thu = daily, fri = daily, sat = daily, sun = daily),
            ),
            StoreDto(
                id = "listvyanka", city = "Листвянка", timezone = "Asia/Irkutsk",
                schedule = StoreDto.ScheduleDto(fri = weekend, sat = weekend, sun = weekend),
            ),
        )

        override suspend fun product(id: String) = ProductDto()
        override suspend fun home() = HomeContentDto()
        override suspend fun story() = StoryContentDto()
        override suspend fun onboarding() = OnboardingContentDto()
        override suspend fun checkout() = CheckoutContentDto()
        override suspend fun care() = CareContentDto()
    }
}
