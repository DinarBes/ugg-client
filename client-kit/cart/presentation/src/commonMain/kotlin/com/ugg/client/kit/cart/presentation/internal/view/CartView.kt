package com.ugg.client.kit.cart.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.cart.domain.entities.Cart
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

@Composable
internal fun CartView(
    cart: Cart?,
    onBack: () -> Unit,
    onChooseModel: () -> Unit,
    onChangeQuantity: (lineId: String, delta: Int) -> Unit,
    onAddExtra: (String) -> Unit,
    onSelectDelivery: (String) -> Unit,
    onGiftWrapChange: (Boolean) -> Unit,
    onCheckout: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(UggTheme.colors.background)) {
        TopBar(subtitle = cart?.takeUnless { it.isEmpty }?.subtitle, onBack = onBack)

        when {
            cart == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UggTheme.colors.accent)
            }

            cart.isEmpty -> EmptyCart(onChooseModel = onChooseModel)

            else -> {
                CartContent(
                    cart = cart,
                    onChangeQuantity = onChangeQuantity,
                    onAddExtra = onAddExtra,
                    onSelectDelivery = onSelectDelivery,
                    onGiftWrapChange = onGiftWrapChange,
                    modifier = Modifier.weight(1f),
                )
                CheckoutBar(onCheckout = onCheckout)
            }
        }
    }
}

@Composable
private fun TopBar(subtitle: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UggIconButton(icon = UggIcons.ArrowLeft, contentDescription = "Назад", onClick = onBack)
        Column {
            Text(text = "Корзина", style = UggTheme.typography.displayL, color = UggTheme.colors.content)
            subtitle?.let {
                Text(text = it, style = UggTheme.typography.caption, color = UggTheme.colors.contentSecondary)
            }
        }
    }
}

@Composable
private fun CartContent(
    cart: Cart,
    onChangeQuantity: (String, Int) -> Unit,
    onAddExtra: (String) -> Unit,
    onSelectDelivery: (String) -> Unit,
    onGiftWrapChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
    ) {
        items(cart.lines, key = { it.id }) { line ->
            CartLineCard(line = line, onChangeQuantity = { delta -> onChangeQuantity(line.id, delta) })
        }

        if (cart.extras.isNotEmpty()) {
            item(key = "extras") { ExtrasSection(extras = cart.extras, onAdd = onAddExtra) }
        }

        item(key = "delivery-title") {
            Text(
                text = "Получение",
                style = UggTheme.typography.displayM,
                color = UggTheme.colors.content,
                modifier = Modifier.padding(top = UggTheme.spacing.xs),
            )
        }
        items(cart.deliveryOptions, key = { "delivery-${it.id}" }) { option ->
            DeliveryOptionCard(
                option = option,
                isSelected = option.id == cart.selectedDeliveryId,
                onClick = { onSelectDelivery(option.id) },
            )
        }

        item(key = "gift") {
            GiftWrapRow(
                title = cart.giftWrapTitle,
                description = cart.giftWrapDescription,
                isChecked = cart.isGiftWrapped,
                onCheckedChange = onGiftWrapChange,
            )
        }

        item(key = "summary") { SummaryCard(cart) }
    }
}

@Composable
private fun SummaryCard(cart: Cart) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated, shape)
            .border(1.dp, colors.outline, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        cart.summary.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = row.title, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
                Text(text = row.value, style = UggTheme.typography.bodySmall, color = colors.content)
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.outline)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Итого", style = UggTheme.typography.heading, color = colors.content)
            Text(text = cart.total, style = UggTheme.typography.price, color = colors.content)
        }
    }
}

@Composable
private fun CheckoutBar(onCheckout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        UggButton(text = "Оформить заказ", onClick = onCheckout, modifier = Modifier.fillMaxWidth())
        Text(
            text = "Оплата онлайн или при получении в мастерской",
            style = UggTheme.typography.caption,
            color = UggTheme.colors.contentSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EmptyCart(onChooseModel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = UggTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Корзина пуста", style = UggTheme.typography.displayM, color = UggTheme.colors.content)
        Text(
            text = "Выберите цвет и размер — мастер сошьёт пару за 5–7 дней.",
            style = UggTheme.typography.body,
            color = UggTheme.colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
        UggButton(text = "Выбрать пару", onClick = onChooseModel, modifier = Modifier.padding(top = UggTheme.spacing.xs))
    }
}
