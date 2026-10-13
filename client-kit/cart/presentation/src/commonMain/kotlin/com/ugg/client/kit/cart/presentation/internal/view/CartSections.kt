package com.ugg.client.kit.cart.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.cart.domain.entities.CartExtra
import com.ugg.client.kit.cart.domain.entities.CartLine
import com.ugg.client.kit.cart.domain.entities.DeliveryOption
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

private const val EXTRAS_PER_ROW = 3

@Composable
internal fun CartLineCard(line: CartLine, onChangeQuantity: (Int) -> Unit) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated, shape)
            .border(1.dp, colors.outline, shape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UggPhotoPlaceholder(
            modifier = if (line.isExtra) Modifier.size(72.dp) else Modifier.size(width = 88.dp, height = 104.dp),
            shape = UggTheme.shapes.small,
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = line.title,
                style = if (line.isExtra) UggTheme.typography.heading else UggTheme.typography.displayM,
                color = colors.content,
            )
            Text(text = line.details, style = UggTheme.typography.caption, color = colors.contentSecondary)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = line.price, style = UggTheme.typography.label, color = colors.content)
                QuantityStepper(quantity = line.quantity, onChange = onChangeQuantity)
            }
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .clip(UggTheme.shapes.pill)
            .background(UggTheme.colors.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(icon = UggIcons.Minus, description = "Меньше", onClick = { onChange(-1) })
        Text(text = quantity.toString(), style = UggTheme.typography.label, color = UggTheme.colors.content)
        StepperButton(icon = UggIcons.Plus, description = "Больше", onClick = { onChange(+1) })
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(UggTheme.shapes.pill)
            .background(UggTheme.colors.surfaceElevated)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = UggTheme.colors.content, modifier = Modifier.size(14.dp))
    }
}

@Composable
internal fun ExtrasSection(extras: List<CartExtra>, onAdd: (String) -> Unit) {
    val colors = UggTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.s)) {
        Text(text = "Добавить к заказу", style = UggTheme.typography.heading, color = colors.content)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            extras.take(EXTRAS_PER_ROW).forEach { extra ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAdd(extra.id) },
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    UggPhotoPlaceholder(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = UggTheme.shapes.medium,
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(UggTheme.spacing.xs)
                                .size(28.dp)
                                .clip(UggTheme.shapes.pill)
                                .background(colors.surfaceElevated),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(imageVector = UggIcons.Plus, contentDescription = "Добавить", tint = colors.content, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(text = extra.title, style = UggTheme.typography.caption, color = colors.content)
                    Text(text = extra.price, style = UggTheme.typography.label, color = colors.accentStrong)
                }
            }
            // Keep cards the same width when only one or two offers are left.
            repeat(EXTRAS_PER_ROW - extras.size.coerceAtMost(EXTRAS_PER_ROW)) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
internal fun DeliveryOptionCard(option: DeliveryOption, isSelected: Boolean, onClick: () -> Unit) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.content else colors.outline, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(UggTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
    ) {
        RadioDot(isSelected = isSelected)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = option.title, style = UggTheme.typography.label, color = colors.content)
            Text(text = option.description, style = UggTheme.typography.caption, color = colors.contentSecondary)
        }

        Text(text = option.price, style = UggTheme.typography.label, color = if (isSelected) colors.info else colors.content)
    }
}

@Composable
private fun RadioDot(isSelected: Boolean) {
    val colors = UggTheme.colors

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(UggTheme.shapes.pill)
            .background(if (isSelected) colors.surfaceInverse else colors.surfaceElevated)
            .border(1.5.dp, if (isSelected) colors.surfaceInverse else colors.outline, UggTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(Modifier.size(8.dp).clip(UggTheme.shapes.pill).background(colors.contentInverse))
        }
    }
}

@Composable
internal fun GiftWrapRow(title: String, description: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = UggTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.medium)
            .background(colors.surfaceVariant.copy(alpha = 0.5f))
            .clickable(role = Role.Switch) { onCheckedChange(!isChecked) }
            .padding(UggTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = UggIcons.Gift, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = UggTheme.typography.label, color = colors.content)
            Text(text = description, style = UggTheme.typography.caption, color = colors.contentSecondary)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.surfaceElevated,
                checkedTrackColor = colors.surfaceInverse,
                checkedBorderColor = colors.surfaceInverse,
                uncheckedThumbColor = colors.surfaceElevated,
                uncheckedTrackColor = colors.outline,
                uncheckedBorderColor = colors.outline,
            ),
        )
    }
}
