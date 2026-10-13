package com.ugg.client.kit.profile.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.profile.domain.entities.ActiveOrder

/** Dark card «Ваша пара у мастера» with the sewing progress. */
@Composable
internal fun ActiveOrderCard(order: ActiveOrder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.extraLarge)
            .background(colors.surfaceInverse)
            .clickable(onClick = onClick)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = order.number.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
            Text(text = order.details, style = UggTheme.typography.caption, color = colors.surfaceVariant)
        }
        Text(text = order.title, style = UggTheme.typography.displayL, color = colors.contentInverse)
        Text(
            text = order.description,
            style = UggTheme.typography.bodySmall,
            color = colors.contentInverse.copy(alpha = 0.75f),
        )

        if (order.steps.isNotEmpty()) {
            OrderProgress(steps = order.steps, current = order.currentStep, modifier = Modifier.padding(top = UggTheme.spacing.xs))
        }

        if (order.masterPhotoCount > 0) {
            MasterPhotos(count = order.masterPhotoCount, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun OrderProgress(steps: List<String>, current: Int, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            steps.indices.forEach { index ->
                StepDot(isDone = index < current, isCurrent = index == current)

                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (index < current) colors.accent else colors.contentInverse.copy(alpha = 0.25f)),
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            steps.forEachIndexed { index, step ->
                Text(
                    text = step,
                    style = UggTheme.typography.caption,
                    color = if (index == current) colors.contentInverse else colors.surfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun StepDot(isDone: Boolean, isCurrent: Boolean) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.pill

    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(shape)
            .background(
                when {
                    isDone -> colors.accent
                    isCurrent -> colors.contentInverse
                    else -> colors.surfaceInverse
                },
            )
            .then(
                if (!isDone && !isCurrent) Modifier.border(1.5.dp, colors.contentInverse.copy(alpha = 0.4f), shape) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isDone -> Icon(imageVector = UggIcons.Check, contentDescription = null, tint = colors.surfaceInverse, modifier = Modifier.size(14.dp))
            isCurrent -> Box(Modifier.size(10.dp).clip(shape).background(colors.accent))
        }
    }
}

@Composable
private fun MasterPhotos(count: Int, modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.medium)
            .background(colors.contentInverse.copy(alpha = 0.08f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(minOf(count, 2)) {
            UggPhotoPlaceholder(modifier = Modifier.size(52.dp), shape = UggTheme.shapes.small)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Мастер прислал фото", style = UggTheme.typography.label, color = colors.contentInverse)
            Text(text = "посмотрите, как шьётся ваша пара", style = UggTheme.typography.caption, color = colors.surfaceVariant)
        }
    }
}
