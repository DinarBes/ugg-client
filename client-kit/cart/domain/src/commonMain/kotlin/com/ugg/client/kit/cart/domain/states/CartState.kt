package com.ugg.client.kit.cart.domain.states

import com.ugg.client.kit.cart.domain.entities.Cart

data class CartState(
    val cart: Cart? = null,
    val error: Throwable? = null,
)
