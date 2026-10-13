package com.ugg.client.infrastructure.firebase.firestore.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `orders/{id}`. Items keep a copy of names and prices at the moment of the order, so a later
 * catalog change does not rewrite what the customer bought.
 */
@Serializable
data class OrderDto(
    val id: String = "",
    val userId: String = "",
    /** «Заказ № 1024». */
    val number: Long = 0,
    val status: OrderStatus = OrderStatus.Accepted,
    val items: List<OrderItemDto> = emptyList(),
    val delivery: OrderDeliveryDto = OrderDeliveryDto(),
    val giftWrap: Boolean = false,
    val totals: OrderTotalsDto = OrderTotalsDto(),
    val contactPhone: String = "",
    val comment: String = "",
    /** Epoch milliseconds; `null` until the master gives a date. */
    val estimatedReadyAt: Long? = null,
    /** Process photos from the master, paths in Firebase Storage. */
    val masterPhotos: List<String> = emptyList(),
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

@Serializable
enum class OrderStatus {
    @SerialName("accepted") Accepted,
    @SerialName("cutting") Cutting,
    @SerialName("sewing") Sewing,
    @SerialName("ready") Ready,
    @SerialName("completed") Completed,
    @SerialName("cancelled") Cancelled,
    ;

    val isActive: Boolean
        get() = this != Completed && this != Cancelled

    companion object {
        /** Exactly how the statuses are spelled in Firestore, for queries. */
        val activeNames = listOf("accepted", "cutting", "sewing", "ready")
    }
}

/** [kind] is `boots` or `extra`; options are filled for the boots only. */
@Serializable
data class OrderItemDto(
    val kind: String = KIND_BOOTS,
    val productId: String? = null,
    val extraId: String? = null,
    val title: String = "",
    val shadeId: String? = null,
    val shadeName: String? = null,
    val color: String? = null,
    val heightId: String? = null,
    val size: String? = null,
    val unitPrice: Int = 0,
    val quantity: Int = 1,
) {
    companion object {
        const val KIND_BOOTS = "boots"
        const val KIND_EXTRA = "extra"
    }
}

@Serializable
data class OrderDeliveryDto(
    val id: String = "",
    val title: String = "",
    val price: Int = 0,
    /** For courier and CDEK; `null` for pickup. */
    val address: AddressDto? = null,
    /** For pickup; `null` otherwise. */
    val storeId: String? = null,
)

@Serializable
data class OrderTotalsDto(val items: Int = 0, val delivery: Int = 0, val total: Int = 0)

/** What the app sends to place an order; the number, status and dates are filled in by [OrderService]. */
data class OrderDraft(
    val items: List<OrderItemDto>,
    val delivery: OrderDeliveryDto,
    val giftWrap: Boolean,
    val totals: OrderTotalsDto,
    val contactPhone: String,
    val comment: String = "",
)
