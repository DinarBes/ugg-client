package com.ugg.client.kit.product.presentation.internal.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggColorChip
import com.ugg.client.kit.core.presentation.external.components.UggIconButton
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.components.UggSegment
import com.ugg.client.kit.core.presentation.external.components.UggSegmentedControl
import com.ugg.client.kit.core.presentation.external.components.UggSizeCell
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.entities.ProductDetail
import com.ugg.client.kit.product.domain.entities.ProductFeature
import com.ugg.client.kit.product.domain.entities.ProductSelection
import kotlinx.coroutines.launch

private const val VISIBLE_THUMBNAILS = 4
private const val SIZES_PER_ROW = 6

/** Placeholder gradient tinted with the selected sheepskin color, so switching colors is visible. */
private fun tintedTop(tint: Long?, fallback: Color): Color =
    tint?.let { lerp(Color(it), Color.White, 0.7f) } ?: fallback

private fun tintedBottom(tint: Long?, fallback: Color): Color =
    tint?.let { lerp(Color(it), Color.White, 0.15f) } ?: fallback

@Composable
internal fun ProductGallery(
    pagerState: PagerState,
    tint: Long?,
    onBack: (() -> Unit)?,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val colors = UggTheme.colors
    val buttonColor = colors.surfaceElevated.copy(alpha = 0.85f)

    Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
            UggPhotoPlaceholder(
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape,
                top = tintedTop(tint, colors.surface),
                bottom = tintedBottom(tint, colors.accent),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (onBack != null) {
                UggIconButton(icon = UggIcons.ArrowLeft, contentDescription = "Назад", onClick = onBack, containerColor = buttonColor)
            }
            Spacer(Modifier.weight(1f))
            UggIconButton(icon = UggIcons.Share, contentDescription = "Поделиться", onClick = onShare, containerColor = buttonColor)
            UggIconButton(icon = UggIcons.Heart, contentDescription = "В избранное", onClick = onToggleFavorite, containerColor = buttonColor)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = UggTheme.spacing.l),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(pagerState.pageCount) { index ->
                val isCurrent = index == pagerState.currentPage

                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(if (isCurrent) 22.dp else 6.dp)
                        .clip(UggTheme.shapes.pill)
                        .background(colors.content.copy(alpha = if (isCurrent) 0.9f else 0.25f)),
                )
            }
        }
    }
}

@Composable
internal fun ProductThumbnails(photoCount: Int, pagerState: PagerState, tint: Long?) {
    val colors = UggTheme.colors
    val scope = rememberCoroutineScope()
    val visible = minOf(photoCount, VISIBLE_THUMBNAILS)
    val hidden = photoCount - visible

    if (photoCount <= 1) return

    Row(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(visible) { index ->
            val isCurrent = pagerState.currentPage == index

            UggPhotoPlaceholder(
                modifier = Modifier
                    .size(64.dp)
                    .then(if (isCurrent) Modifier.border(1.5.dp, colors.content, UggTheme.shapes.small) else Modifier)
                    .clip(UggTheme.shapes.small)
                    .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
                shape = UggTheme.shapes.small,
                top = tintedTop(tint, colors.surface),
                bottom = tintedBottom(tint, colors.accent),
            )
        }

        if (hidden > 0) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(UggTheme.shapes.small)
                    .background(colors.surface)
                    .clickable { scope.launch { pagerState.animateScrollToPage(visible) } },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "+$hidden", style = UggTheme.typography.label, color = colors.contentSecondary)
            }
        }
    }
}

@Composable
internal fun ProductSummary(product: Product) {
    val colors = UggTheme.colors

    Column(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal).padding(top = UggTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = product.kicker.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
        Text(text = product.title, style = UggTheme.typography.displayXL, color = colors.content)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = product.price, style = UggTheme.typography.price, color = colors.accentStrong)

            Row(
                modifier = Modifier
                    .clip(UggTheme.shapes.pill)
                    .background(colors.info.copy(alpha = 0.12f))
                    .padding(horizontal = UggTheme.spacing.s, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).clip(UggTheme.shapes.pill).background(colors.info))
                Text(text = product.leadTime, style = UggTheme.typography.caption, color = colors.info)
            }
        }

        Text(text = product.description, style = UggTheme.typography.body, color = colors.contentSecondary)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = colors.outline)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProductOptions(
    product: Product,
    selection: ProductSelection,
    onSelectShade: (String) -> Unit,
    onSelectHeight: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onOpenSizeChart: () -> Unit,
) {
    val colors = UggTheme.colors
    val typography = UggTheme.typography
    val shadeName = product.shades.firstOrNull { it.id == selection.shadeId }?.name

    Column(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (product.shades.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Цвет", style = typography.heading, color = colors.content)
                shadeName?.let { Text(text = "· $it", style = typography.heading, color = colors.contentSecondary) }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
                verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
            ) {
                product.shades.forEach { shade ->
                    UggColorChip(
                        name = shade.name,
                        color = Color(shade.color),
                        isSelected = shade.id == selection.shadeId,
                        onClick = { onSelectShade(shade.id) },
                    )
                }
            }
        }

        if (product.heights.size > 1) {
            Spacer(Modifier.height(6.dp))
            Text(text = "Высота голенища", style = typography.heading, color = colors.content)
            UggSegmentedControl(
                segments = product.heights.map { UggSegment(it.name, it.value) },
                selectedIndex = product.heights.indexOfFirst { it.id == selection.heightId },
                onSelect = { onSelectHeight(product.heights[it].id) },
            )
        }

        if (product.sizes.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Размер", style = typography.heading, color = colors.content)
                Text(
                    text = "Таблица размеров",
                    style = typography.label,
                    color = colors.accent,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clip(UggTheme.shapes.small).clickable(onClick = onOpenSizeChart),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
                product.sizes.chunked(SIZES_PER_ROW).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs)) {
                        row.forEach { size ->
                            UggSizeCell(
                                label = size.label,
                                isSelected = size.label == selection.size,
                                isEnabled = size.isAvailable,
                                onClick = { onSelectSize(size.label) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // Keep the last row's cells the same width as the full rows above.
                        repeat(SIZES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            product.customSizeHint?.let { hint ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(UggTheme.shapes.medium)
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = UggTheme.spacing.s),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(imageVector = UggIcons.Ruler, contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(20.dp))
                    Text(text = hint, style = typography.bodySmall, color = colors.content)
                }
            }
        }
    }
}

@Composable
internal fun ProductFeatures(features: List<ProductFeature>) {
    val colors = UggTheme.colors

    Column(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = "Почему они тёплые", style = UggTheme.typography.displayM, color = colors.content)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            features.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { feature ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(UggTheme.shapes.medium)
                                .background(colors.surface)
                                .padding(UggTheme.spacing.m),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = feature.kind.icon(),
                                contentDescription = null,
                                tint = colors.accentStrong,
                                modifier = Modifier.size(22.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = feature.title, style = UggTheme.typography.label, color = colors.content)
                                Text(text = feature.caption, style = UggTheme.typography.caption, color = colors.contentSecondary)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private fun ProductFeature.Kind.icon() = when (this) {
    ProductFeature.Kind.Material -> UggIcons.Leaf
    ProductFeature.Kind.Handmade -> UggIcons.Scissors
    ProductFeature.Kind.Warmth -> UggIcons.Snowflake
    ProductFeature.Kind.Warranty -> UggIcons.ShieldCheck
}

@Composable
internal fun ProductDetails(details: List<ProductDetail>, expanded: Int?, onToggle: (Int) -> Unit) {
    val colors = UggTheme.colors

    Column(
        modifier = Modifier
            .padding(horizontal = UggTheme.spacing.screenHorizontal)
            .padding(top = UggTheme.spacing.s, bottom = UggTheme.spacing.xl),
    ) {
        details.forEachIndexed { index, detail ->
            val isExpanded = index == expanded

            HorizontalDivider(thickness = 1.dp, color = colors.outline)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(index) }
                    .padding(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = detail.title, style = UggTheme.typography.heading, color = colors.content)
                    Icon(
                        imageVector = if (isExpanded) UggIcons.ChevronUp else UggIcons.ChevronDown,
                        contentDescription = null,
                        tint = colors.content,
                        modifier = Modifier.size(20.dp),
                    )
                }
                AnimatedVisibility(visible = isExpanded) {
                    Text(text = detail.body, style = UggTheme.typography.bodySmall, color = colors.contentSecondary)
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.outline)
    }
}
