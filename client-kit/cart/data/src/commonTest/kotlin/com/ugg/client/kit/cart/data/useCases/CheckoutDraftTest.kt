package com.ugg.client.kit.cart.data.useCases

import com.ugg.client.infrastructure.firebase.firestore.dto.OrderItemDto
import com.ugg.client.kit.cart.data.storage.CartSnapshot
import com.ugg.client.kit.cart.domain.entities.CartProductOptions
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckoutDraftTest {
    private val snapshot = CartSnapshot(
        lines = listOf(
            CartSnapshot.Line(
                id = "classic-chestnut-standard-38",
                title = "Классика",
                details = "Каштан · 38 · Стандарт",
                unitPrice = 14900,
                quantity = 1,
                isExtra = false,
                color = 0xFFB98A5E,
                options = CartProductOptions("classic", "chestnut", "Каштан", "standard", "38"),
            ),
            CartSnapshot.Line("care-cream", "Крем для овчины", "уход", 890, quantity = 2, isExtra = true),
        ),
        deliveryId = "courier",
        isGiftWrapped = true,
    )

    @Test
    fun cartBecomesAnOrderDraft() {
        val draft = snapshot.toDraft(contactPhone = "+79001234567", address = null)

        val boots = draft.items.first()
        assertEquals(OrderItemDto.KIND_BOOTS, boots.kind)
        assertEquals("chestnut", boots.shadeId)
        assertEquals("#B98A5E", boots.color)
        assertEquals("38", boots.size)

        val cream = draft.items.last()
        assertEquals(OrderItemDto.KIND_EXTRA, cream.kind)
        assertEquals("care-cream", cream.extraId)

        assertEquals("courier", draft.delivery.id)
        assertEquals(14900 + 2 * 890, draft.totals.items)
        assertEquals(14900 + 2 * 890 + 350, draft.totals.total)
        assertEquals(true, draft.giftWrap)
    }
}
