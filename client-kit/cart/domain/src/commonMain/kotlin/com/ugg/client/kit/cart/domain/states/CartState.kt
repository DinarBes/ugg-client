package com.ugg.client.kit.cart.domain.states

import com.ugg.client.kit.cart.domain.entities.Cart

data class CartState(
    val cart: Cart? = null,
    val error: Throwable? = null,
    val isCheckingOut: Boolean = false,
    /** Set once an order is placed: the screen shows «Заказ № … оформлен». */
    val placedOrderNumber: Long? = null,
    /** A guest pressed «Оформить заказ»: the screen sends them to the phone step, then clears this. */
    val isSignInRequired: Boolean = false,
)
