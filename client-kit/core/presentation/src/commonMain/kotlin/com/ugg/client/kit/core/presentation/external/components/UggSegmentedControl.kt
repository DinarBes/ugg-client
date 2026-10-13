package com.ugg.client.kit.core.presentation.external.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.theme.UggTheme

data class UggSegment(
    val title: String,
    val subtitle: String? = null,
)

/** Two-or-more option switch on a cream track; the selected option is a raised white pill. */
@Composable
fun UggSegmentedControl(
    segments: List<UggSegment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.small

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(UggTheme.shapes.medium)
            .background(colors.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        segments.forEachIndexed { index, segment ->
            val isSelected = index == selectedIndex

            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(if (isSelected) Modifier.shadow(2.dp, shape) else Modifier)
                    .clip(shape)
                    .background(if (isSelected) colors.surfaceElevated else colors.surface)
                    .clickable(role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(horizontal = UggTheme.spacing.s, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = segment.title,
                    style = UggTheme.typography.label,
                    color = if (isSelected) colors.content else colors.contentSecondary,
                )
                segment.subtitle?.let {
                    Text(text = it, style = UggTheme.typography.caption, color = colors.contentSecondary)
                }
            }
        }
    }
}
