package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDeliveryDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDraft
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderItemDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderStatus
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderTotalsDto
import com.ugg.client.infrastructure.firebase.firestore.services.FirestoreOrderService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderServiceTest {

    @Test
    fun placingTakesTheNextNumberAndMovesTheCounter() = runTest {
        val store = FakeDocumentStore(mapOf("counters/orders" to mapOf("next" to 1024L)))
        val service = FirestoreOrderService(store)

        val first = service.placeOrder(USER, DRAFT)
        val second = service.placeOrder(USER, DRAFT)

        assertEquals(1024L, first.number)
        assertEquals(1025L, second.number)
        assertEquals(1026L, store.documents.value.getValue("counters/orders")["next"])
        assertEquals(OrderStatus.Accepted, first.status)
        assertEquals(FakeDocumentStore.NOW, first.createdAt)
        assertEquals("Каштан", first.items.single().shadeName)
        // The id is the document path, not a field inside it.
        assertEquals(false, "id" in store.documents.value.getValue("orders/${first.id}"))
    }

    @Test
    fun placingWithoutCounterFails() = runTest {
        val service = FirestoreOrderService(FakeDocumentStore())

        val error = assertFailsWith<FirestoreException> { service.placeOrder(USER, DRAFT) }

        assertEquals(FirestoreException.Reason.NotFound, error.reason)
    }

    @Test
    fun activeOrdersAreTheUsersUnfinishedOnesNewestFirst() = runTest {
        val service = FirestoreOrderService(
            FakeDocumentStore(
                mapOf(
                    "orders/a" to order(USER, "sewing", createdAt = 1),
                    "orders/b" to order(USER, "completed", createdAt = 2),
                    "orders/c" to order("someone-else", "accepted", createdAt = 3),
                    "orders/d" to order(USER, "accepted", createdAt = 4),
                ),
            ),
        )

        val active = service.observeActiveOrders(USER).first()

        assertEquals(listOf("d", "a"), active.map { it.id })
        assertEquals(OrderStatus.Sewing, active[1].status)
    }

    private fun order(userId: String, status: String, createdAt: Long) =
        mapOf("userId" to userId, "status" to status, "number" to createdAt, "createdAt" to createdAt)

    private companion object {
        const val USER = "user-1"
        val DRAFT = OrderDraft(
            items = listOf(
                OrderItemDto(productId = "classic", title = "Классика", shadeId = "chestnut", shadeName = "Каштан", size = "38", unitPrice = 14900),
            ),
            delivery = OrderDeliveryDto(id = "pickup", title = "Самовывоз", storeId = "irkutsk"),
            giftWrap = false,
            totals = OrderTotalsDto(items = 14900, delivery = 0, total = 14900),
            contactPhone = "+79000000000",
        )
    }
}
