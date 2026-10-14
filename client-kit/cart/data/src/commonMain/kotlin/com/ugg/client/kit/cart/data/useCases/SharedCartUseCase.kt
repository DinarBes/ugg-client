package com.ugg.client.kit.cart.data.useCases

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDeliveryDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDraft
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderItemDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderTotalsDto
import com.ugg.client.infrastructure.firebase.firestore.services.OrderService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.cart.data.mappers.CartCatalog
import com.ugg.client.kit.cart.data.mappers.GIFT_WRAP_PRICE
import com.ugg.client.kit.cart.data.storage.CartSnapshot
import com.ugg.client.kit.cart.data.storage.CartStorage
import com.ugg.client.kit.cart.domain.exceptions.SignInRequiredException
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import com.ugg.client.kit.core.data.content.translatingContentErrors
import kotlinx.coroutines.flow.first

/**
 * The device cart ([MockCartUseCase] does all the editing) plus a real checkout: the cart becomes
 * `orders/{id}` in Firestore, numbered from `counters/orders`, and shows up in the profile and in
 * the admin app. No payment yet — the order is placed as is.
 */
class SharedCartUseCase(
    private val storage: CartStorage,
    private val phoneAuth: PhoneAuthService,
    private val orders: OrderService,
    private val users: UserService,
) : CartUseCase by MockCartUseCase(storage) {

    override suspend fun checkout(): Long {
        val user = phoneAuth.currentUser ?: throw SignInRequiredException()
        val snapshot = storage.snapshot.value
        if (snapshot.lines.isEmpty()) error("The cart is empty")

        val order = translatingContentErrors {
            val draft = snapshot.toDraft(contactPhone = user.phone.orEmpty(), address = deliveryAddress(user.uid, snapshot))
            orders.placeOrder(user.uid, draft)
        }

        storage.update { CartSnapshot() }
        return order.number
    }

    /** Courier and CDEK go to the default saved address (or the first one); pickup needs none. */
    private suspend fun deliveryAddress(uid: String, snapshot: CartSnapshot): AddressDto? {
        if (snapshot.deliveryId == CartSnapshot.DEFAULT_DELIVERY_ID) return null
        val addresses = users.observeAddresses(uid).first()
        return addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
    }
}

internal fun CartSnapshot.toDraft(contactPhone: String, address: AddressDto?): OrderDraft {
    val delivery = CartCatalog.deliveries.firstOrNull { it.id == deliveryId } ?: CartCatalog.deliveries.first()
    val itemsTotal = lines.sumOf { it.unitPrice * it.quantity }
    val giftWrapPrice = if (isGiftWrapped) GIFT_WRAP_PRICE else 0

    return OrderDraft(
        items = lines.map { line ->
            if (line.isExtra) {
                OrderItemDto(
                    kind = OrderItemDto.KIND_EXTRA,
                    extraId = line.id,
                    title = line.title,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity,
                )
            } else {
                OrderItemDto(
                    kind = OrderItemDto.KIND_BOOTS,
                    productId = line.options?.productId,
                    title = line.title,
                    shadeId = line.options?.shadeId,
                    shadeName = line.options?.shadeName,
                    color = line.color?.toHex(),
                    heightId = line.options?.heightId,
                    size = line.options?.size,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity,
                )
            }
        },
        delivery = OrderDeliveryDto(
            id = delivery.id,
            title = delivery.title,
            price = delivery.price,
            address = address,
            storeId = if (delivery.id == CartSnapshot.DEFAULT_DELIVERY_ID) PICKUP_STORE_ID else null,
        ),
        giftWrap = isGiftWrapped,
        totals = OrderTotalsDto(items = itemsTotal, delivery = delivery.price, total = itemsTotal + delivery.price + giftWrapPrice),
        contactPhone = contactPhone,
    )
}

/** Pickup is from the Irkutsk workshop, the same store the cart describes. */
private const val PICKUP_STORE_ID = "irkutsk"

/** `0xFFB98A5E` → `"#B98A5E"`, the form Firestore keeps colors in. */
private fun Long.toHex(): String = "#" + (this and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')
