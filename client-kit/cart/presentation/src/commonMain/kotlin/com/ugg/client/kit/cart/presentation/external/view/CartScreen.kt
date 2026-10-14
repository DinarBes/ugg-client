package com.ugg.client.kit.cart.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.cart.presentation.external.view.models.CartViewModel
import com.ugg.client.kit.cart.presentation.internal.view.CartView
import com.ugg.client.kit.core.presentation.external.components.UggAlertDialog
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * The cart, full screen over the tabs; handles system bar insets itself.
 *
 * @param onChooseModel «Выбрать пару» on the empty cart.
 * @param onSignIn a guest pressed «Оформить заказ»: open the phone step; the cart keeps its lines.
 * @param onOpenOrders «Мои заказы» after an order is placed.
 */
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onChooseModel: () -> Unit,
    onSignIn: () -> Unit,
    onOpenOrders: () -> Unit,
) {
    val viewModel: CartViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSignInRequired) {
        if (state.isSignInRequired) {
            viewModel.onCheckoutHandled()
            onSignIn()
        }
    }

    CartView(
        cart = state.cart,
        onBack = onBack,
        onChooseModel = onChooseModel,
        onChangeQuantity = { lineId, delta -> viewModel.changeQuantity(lineId, delta) },
        onAddExtra = { viewModel.addExtra(it) },
        onSelectDelivery = { viewModel.selectDelivery(it) },
        onGiftWrapChange = { viewModel.setGiftWrap(it) },
        onCheckout = { viewModel.checkout() },
        isCheckingOut = state.isCheckingOut,
        error = state.error,
    )

    state.placedOrderNumber?.let { number ->
        UggAlertDialog(
            title = "Заказ № $number оформлен",
            text = "Мастер свяжется с вами, чтобы уточнить детали. Этапы пошива — в профиле.",
            confirmText = "Мои заказы",
            dismissText = "Закрыть",
            onConfirm = {
                viewModel.onCheckoutHandled()
                onOpenOrders()
            },
            onDismiss = {
                viewModel.onCheckoutHandled()
                onBack()
            },
        )
    }
}
