package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.entities.Address
import com.ugg.client.kit.profile.domain.states.AddressesState

@Composable
internal fun AddressesView(
    state: AddressesState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
) {
    FormScaffold(
        title = "Адреса доставки",
        onBack = onBack,
        isLoading = state.isLoading && state.addresses.isEmpty(),
        loadError = state.error,
        onRetry = onRetry,
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        bottom = {
            UggButton(text = "Добавить адрес", onClick = onAdd, showArrow = false, modifier = Modifier.fillMaxWidth())
        },
    ) {
        if (state.addresses.isEmpty()) {
            EmptyAddresses()
        } else {
            state.addresses.forEach { address -> AddressCard(address, onClick = { onOpen(address.id) }) }
        }
    }
}

@Composable
private fun AddressCard(address: Address, onClick: () -> Unit) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(if (address.isDefault) 1.5.dp else 1.dp, if (address.isDefault) colors.accent else colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(UggTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = UggIcons.Pin, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (address.isDefault) {
                Text(text = "Основной".uppercase(), style = UggTheme.typography.overline, color = colors.accent)
            }
            Text(text = address.summary, style = UggTheme.typography.label, color = colors.content)
            if (address.comment.isNotBlank()) {
                Text(text = address.comment, style = UggTheme.typography.caption, color = colors.contentSecondary)
            }
        }
        Icon(imageVector = UggIcons.ChevronRight, contentDescription = null, tint = colors.contentSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyAddresses() {
    val colors = UggTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = UggTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
    ) {
        Icon(imageVector = UggIcons.Pin, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(32.dp))
        Text(text = "Пока нет адресов", style = UggTheme.typography.heading, color = colors.content)
        Text(
            text = "Добавьте адрес — при заказе с доставкой он подставится сам.",
            style = UggTheme.typography.body,
            color = colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
