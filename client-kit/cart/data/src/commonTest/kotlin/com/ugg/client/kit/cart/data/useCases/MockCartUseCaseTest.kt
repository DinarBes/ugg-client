package com.ugg.client.kit.cart.data.useCases

import com.ugg.client.kit.cart.data.storage.CartStorage
import com.ugg.client.kit.cart.domain.entities.CartProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockCartUseCaseTest {
    private val boots = CartProduct(id = "classic-chestnut-38", title = "Классика", details = "Каштан · 38", unitPrice = 14900)

    @Test
    fun addingTheSamePairTwiceRaisesTheQuantity() = runTest {
        val useCase = MockCartUseCase(CartStorage())

        useCase.addProduct(boots)
        useCase.addProduct(boots)

        val cart = useCase.observeCart().first()
        assertEquals(1, cart.lines.size)
        assertEquals(2, cart.itemCount)
        assertEquals("29 800 ₽", cart.lines.single().price)
    }

    @Test
    fun quantityDownToZeroRemovesTheLine() = runTest {
        val useCase = MockCartUseCase(CartStorage())
        useCase.addProduct(boots)

        useCase.changeQuantity(boots.id, -1)

        assertTrue(useCase.observeCart().first().isEmpty)
    }

    @Test
    fun addedExtraLeavesTheOffers() = runTest {
        val useCase = MockCartUseCase(CartStorage())

        useCase.addExtra("care-cream")

        val cart = useCase.observeCart().first()
        assertTrue(cart.lines.any { it.id == "care-cream" && it.isExtra })
        assertTrue(cart.extras.none { it.id == "care-cream" })
    }

    @Test
    fun paidDeliveryIsAddedToTheTotal() = runTest {
        val useCase = MockCartUseCase(CartStorage())
        useCase.addProduct(boots)

        useCase.selectDelivery("courier")

        assertEquals("15 250 ₽", useCase.observeCart().first().total)
    }

    @Test
    fun twoScreensSeeTheSameCart() = runTest {
        val storage = CartStorage()

        MockCartUseCase(storage).addProduct(boots)

        assertEquals(1, MockCartUseCase(storage).observeCart().first().itemCount)
    }
}
