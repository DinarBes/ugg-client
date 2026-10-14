package com.ugg.client.kit.product.data

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import com.ugg.client.kit.product.data.useCases.SharedProductUseCase
import com.ugg.client.kit.product.domain.entities.ProductFeature
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SharedProductUseCaseTest {

    @Test
    fun productIsFormattedForTheScreen() = runTest {
        val product = SharedProductUseCase(FakeCatalog(PRODUCT)).fetch()

        assertEquals("classic", product.id)
        assertEquals("14 900 ₽", product.price)
        assertEquals(14900, product.priceValue)
        assertEquals("Сошьём за 5–7 дней", product.leadTime)
        assertEquals(2, product.photoCount)
        assertEquals(0xFFB98A5E, product.shades.single().color)
        assertEquals("22 см", product.heights.single().value)
        assertEquals(listOf(true, false), product.sizes.map { it.isAvailable })
        assertEquals(listOf(ProductFeature.Kind.Warranty, ProductFeature.Kind.Material), product.features.map { it.kind })
        assertNull(product.customSizeHint)
    }

    @Test
    fun noNetworkBecomesNoConnection() = runTest {
        val catalog = FakeCatalog(failure = FirestoreException(FirestoreException.Reason.Unavailable))

        assertFailsWith<NoConnectionException> { SharedProductUseCase(catalog).fetch() }
    }

    private class FakeCatalog(
        private val product: ProductDto = ProductDto(),
        private val failure: Exception? = null,
    ) : CatalogService {
        override suspend fun product(id: String) = failure?.let { throw it } ?: product.copy(id = id)
        override suspend fun stores() = emptyList<StoreDto>()
        override suspend fun home() = HomeContentDto()
        override suspend fun story() = StoryContentDto()
        override suspend fun onboarding() = OnboardingContentDto()
        override suspend fun checkout() = CheckoutContentDto()
    }

    private companion object {
        val PRODUCT = ProductDto(
            title = "Классика",
            price = 14900,
            leadTimeDays = ProductDto.LeadTimeDto(min = 5, max = 7),
            photos = listOf("products/classic/chestnut/1.jpg", "products/classic/chestnut/2.jpg"),
            shades = listOf(ProductDto.ShadeDto(id = "chestnut", name = "Каштан", color = "#B98A5E")),
            heights = listOf(ProductDto.HeightDto(id = "standard", name = "Стандарт", heightCm = 22)),
            sizes = listOf(ProductDto.SizeDto("38", available = true), ProductDto.SizeDto("44", available = false)),
            customSizeHint = " ",
            features = listOf(
                ProductDto.FeatureDto(kind = "warranty", title = "Гарантия на шов"),
                ProductDto.FeatureDto(kind = "something-new", title = "Новое"),
            ),
        )
    }
}
