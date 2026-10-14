package com.ugg.client.kit.product.domain.states

import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.entities.ProductSelection

data class ProductState(
    val product: Product? = null,
    val selection: ProductSelection = ProductSelection(),
    /** Index in [Product.details]; `null` — everything collapsed. */
    val expandedDetail: Int? = 0,
    val isLoading: Boolean = false,
    /** Pull-to-refresh in progress: the content stays on screen under the spinner. */
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)
