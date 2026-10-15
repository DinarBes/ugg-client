package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.core.presentation.resources.Res
import com.ugg.client.kit.core.presentation.resources.app_icon
import org.jetbrains.compose.resources.painterResource

/**
 * The [UggTabBar] of wide windows: a sidebar along the start edge with the brand on top and the
 * same items below, icon (in its own tile) and label in a row. The layout owner picks one or the
 * other by window size.
 */
@Composable
fun UggNavigationRail(
    items: List<UggTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors
    val spacing = UggTheme.spacing

    Row(modifier = modifier.fillMaxHeight().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(248.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = spacing.m, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Brand(modifier = Modifier.padding(start = spacing.xs, bottom = spacing.xxl))

            items.forEachIndexed { index, item ->
                RailItem(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = { onSelect(index) },
                )
            }
        }

        VerticalDivider(thickness = 1.dp, color = colors.outline)
    }
}

/** The app icon — the same as on the Android home screen and in the browser tab — and the wordmark. */
@Composable
private fun Brand(modifier: Modifier = Modifier) {
    val colors = UggTheme.colors

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m)) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)
        )
        Column {
            Text(
                text = "БАЙКАЛ",
                style = UggTheme.typography.displayM.copy(fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = 0.28.em),
                color = colors.content,
            )
            Text(
                text = "угги ручной работы",
                style = UggTheme.typography.caption,
                color = colors.contentSecondary,
            )
        }
    }
}

@Composable
private fun RailItem(item: UggTabItem, isSelected: Boolean, onClick: () -> Unit) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Clip first, so the selected background and the press ripple take the rounded shape.
            .clip(shape)
            .background(if (isSelected) colors.surface else colors.background)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = UggTheme.spacing.xs, vertical = UggTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.s),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(UggTheme.shapes.small)
                .background(if (isSelected) colors.surfaceInverse else colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) colors.contentInverse else colors.contentSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = item.label,
            style = UggTheme.typography.label.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium),
            color = if (isSelected) colors.content else colors.contentSecondary,
        )
    }
}
