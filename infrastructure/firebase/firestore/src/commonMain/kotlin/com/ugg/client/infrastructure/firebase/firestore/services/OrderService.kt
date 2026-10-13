package com.ugg.client.infrastructure.firebase.firestore.services

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDraft
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderStatus
import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.FirestoreJson
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.ServerTimestamp
import com.ugg.client.infrastructure.firebase.firestore.internal.decode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** `orders/{id}`. The customer creates and reads their own orders; statuses are moved by the workshop. */
interface OrderService {
    /** Newest first. */
    fun observeOrders(uid: String): Flow<List<OrderDto>>

    /** Orders not completed or cancelled yet, newest first — the card on the profile. */
    fun observeActiveOrders(uid: String): Flow<List<OrderDto>>

    /** Takes the next number from `counters/orders` and saves the order in one transaction. */
    suspend fun placeOrder(uid: String, draft: OrderDraft): OrderDto
}

internal class FirestoreOrderService(
    private val store: DocumentStore,
) : OrderService {

    override fun observeOrders(uid: String): Flow<List<OrderDto>> = observe(
        RawQuery(
            collection = ORDERS,
            filters = listOf(RawQuery.Filter.EqualTo("userId", uid)),
            orderBy = "createdAt",
            descending = true,
        ),
    )

    override fun observeActiveOrders(uid: String): Flow<List<OrderDto>> = observe(
        RawQuery(
            collection = ORDERS,
            filters = listOf(
                RawQuery.Filter.EqualTo("userId", uid),
                RawQuery.Filter.In("status", OrderStatus.activeNames),
            ),
            orderBy = "createdAt",
            descending = true,
        ),
    )

    override suspend fun placeOrder(uid: String, draft: OrderDraft): OrderDto {
        val id = store.newId(ORDERS)

        val number = store.transaction { transaction ->
            val counter = transaction.get(COUNTER)
                ?: throw FirestoreException(FirestoreException.Reason.NotFound)
            val number = (counter.data[COUNTER_FIELD] as? Number)?.toLong()
                ?: throw FirestoreException(FirestoreException.Reason.Malformed)

            val order = OrderDto(
                userId = uid,
                number = number,
                status = OrderStatus.Accepted,
                items = draft.items,
                delivery = draft.delivery,
                giftWrap = draft.giftWrap,
                totals = draft.totals,
                contactPhone = draft.contactPhone,
                comment = draft.comment,
            )
            val data = FirestoreJson.encode(OrderDto.serializer(), order) +
                mapOf("createdAt" to ServerTimestamp, "updatedAt" to ServerTimestamp)

            transaction.set("$ORDERS/$id", data)
            transaction.set(COUNTER, mapOf(COUNTER_FIELD to number + 1))
            number
        }

        // Read back so the dates are the server's, not guessed on the device.
        return store.get("$ORDERS/$id")?.decode(OrderDto.serializer())
            ?: OrderDto(id = id, userId = uid, number = number)
    }

    private fun observe(query: RawQuery) =
        store.observe(query).map { documents -> documents.map { it.decode(OrderDto.serializer()) } }

    private companion object {
        const val ORDERS = "orders"
        const val COUNTER = "counters/orders"
        const val COUNTER_FIELD = "next"
    }
}
