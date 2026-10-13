package com.ugg.client.kit.cart.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.cart.presentation.external.view.models.CartViewModel
import com.ugg.client.kit.cart.presentation.internal.view.CartView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * The cart, full screen over the tabs; handles system bar insets itself.
 *
 * @param onChooseModel «Выбрать пару» on the empty cart.
 * @param onCheckout «Оформить заказ».
 */
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onChooseModel: () -> Unit,
    onCheckout: () -> Unit,
) {
    val viewModel: CartViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    CartView(
        cart = state.cart,
        onBack = onBack,
        onChooseModel = onChooseModel,
        onChangeQuantity = { lineId, delta -> viewModel.changeQuantity(lineId, delta) },
        onAddExtra = { viewModel.addExtra(it) },
        onSelectDelivery = { viewModel.selectDelivery(it) },
        onGiftWrapChange = { viewModel.setGiftWrap(it) },
        onCheckout = onCheckout,
    )
}
