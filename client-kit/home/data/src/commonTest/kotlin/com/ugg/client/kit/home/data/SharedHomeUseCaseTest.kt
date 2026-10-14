@file:OptIn(ExperimentalTime::class)

package com.ugg.client.kit.home.data

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.CareContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.domain.exceptions.ContentUnavailableException
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import com.ugg.client.kit.home.data.useCases.SharedHomeUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SharedHomeUseCaseTest {
    // 12:00 in Irkutsk on a Wednesday.
    private val noon = Instant.parse("2026-10-14T04:00:00Z")

    @Test
    fun homeIsAssembledFromContentProductAndStores() = runTest {
        val home = SharedHomeUseCase(FakeCatalog(), now = { noon }).fetch()

        assertEquals("Классика", home.hero.title)
        assertEquals("Единственная модель", home.hero.kicker)
        assertEquals("14 900 ₽", home.hero.price)
        assertEquals("шьём под заказ за 5–7 дней", home.hero.leadTime)
        assertEquals(0xFFB98A5E, home.shades.single().color)
        assertEquals(false, home.shades.single().isInStock)
        assertEquals("3 минуты чтения", home.care?.readTime)
        assertEquals(listOf("до 20:00", "откроется в пт"), home.stores.map { it.hours })
        assertEquals(listOf(true, false), home.stores.map { it.isOpen })
    }

    @Test
    fun noNetworkBecomesNoConnection() = runTest {
        val catalog = FakeCatalog(failure = FirestoreException(FirestoreException.Reason.Unavailable))

        assertFailsWith<NoConnectionException> { SharedHomeUseCase(catalog, now = { noon }).fetch() }
    }

    @Test
    fun missingContentBecomesContentUnavailable() = runTest {
        val catalog = FakeCatalog(failure = FirestoreException(FirestoreException.Reason.NotFound))

        val error = assertFailsWith<ContentUnavailableException> { SharedHomeUseCase(catalog, now = { noon }).fetch() }
        assertEquals("Не удалось загрузить. Попробуйте ещё раз", error.message)
    }

    private class FakeCatalog(private val failure: Exception? = null) : CatalogService {
        private val hours = StoreDto.HoursDto("10:00", "20:00")

        override suspend fun home() = failure?.let { throw it } ?: HomeContentDto(
            heroKicker = "Единственная модель",
            care = HomeContentDto.CareDto(kicker = "Уход", title = "Как сохранить угги", readMinutes = 3),
        )

        override suspend fun product(id: String) = ProductDto(
            id = id,
            title = "Классика",
            price = 14900,
            leadTimeDays = ProductDto.LeadTimeDto(min = 5, max = 7),
            shades = listOf(ProductDto.ShadeDto(id = "chestnut", name = "Каштан", color = "#B98A5E", inStock = false)),
        )

        override suspend fun stores() = listOf(
            StoreDto(
                id = "irkutsk", city = "Иркутск", timezone = "Asia/Irkutsk",
                schedule = StoreDto.ScheduleDto(mon = hours, tue = hours, wed = hours, thu = hours, fri = hours, sat = hours, sun = hours),
            ),
            StoreDto(
                id = "listvyanka", city = "Листвянка", timezone = "Asia/Irkutsk",
                schedule = StoreDto.ScheduleDto(fri = hours, sat = hours, sun = hours),
            ),
        )

        override suspend fun story() = StoryContentDto()
        override suspend fun onboarding() = OnboardingContentDto()
        override suspend fun checkout() = CheckoutContentDto()
        override suspend fun care() = CareContentDto()
    }
}
