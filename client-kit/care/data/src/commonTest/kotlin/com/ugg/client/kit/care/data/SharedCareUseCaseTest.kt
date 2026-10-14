package com.ugg.client.kit.care.data

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.CareContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.care.data.useCases.SharedCareUseCase
import com.ugg.client.kit.care.domain.entities.CareTip
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SharedCareUseCaseTest {

    @Test
    fun careIsFormattedForTheScreen() = runTest {
        val care = SharedCareUseCase(
            FakeCatalog(
                CareContentDto(
                    title = "Как сохранить угги",
                    readMinutes = 3,
                    tips = listOf(
                        CareContentDto.TipDto(kind = "drying", title = "Сушка"),
                        CareContentDto.TipDto(kind = "something-new", title = "Новое"),
                    ),
                    donts = listOf("Стирать в машине", " "),
                    contact = CareContentDto.ContactDto(title = "Нужен совет?", body = "Напишите мастеру"),
                ),
            ),
        ).fetch()

        assertEquals("3 минуты чтения", care.readTime)
        assertEquals(listOf(CareTip.Kind.Drying, CareTip.Kind.Other), care.tips.map { it.kind })
        assertEquals(listOf("Стирать в машине"), care.donts)
        assertEquals("Чего не делать", care.dontsTitle)
        assertEquals("Написать в мастерскую", care.contact?.button)
    }

    @Test
    fun emptyContactCardIsHidden() = runTest {
        val care = SharedCareUseCase(FakeCatalog(CareContentDto(contact = CareContentDto.ContactDto()))).fetch()

        assertNull(care.contact)
        assertEquals("", care.readTime)
    }

    @Test
    fun noNetworkBecomesNoConnection() = runTest {
        val catalog = FakeCatalog(failure = FirestoreException(FirestoreException.Reason.Unavailable))

        assertFailsWith<NoConnectionException> { SharedCareUseCase(catalog).fetch() }
    }

    private class FakeCatalog(
        private val care: CareContentDto = CareContentDto(),
        private val failure: Exception? = null,
    ) : CatalogService {
        override suspend fun care() = failure?.let { throw it } ?: care
        override suspend fun product(id: String) = ProductDto()
        override suspend fun stores() = emptyList<StoreDto>()
        override suspend fun home() = HomeContentDto()
        override suspend fun story() = StoryContentDto()
        override suspend fun onboarding() = OnboardingContentDto()
        override suspend fun checkout() = CheckoutContentDto()
    }
}
