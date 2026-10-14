package com.ugg.client.kit.product.presentation.internal.view

import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.WindowInsets
import com.ugg.client.kit.core.presentation.external.components.UggPullToRefreshBox
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.product.domain.entities.Product
import com.ugg.client.kit.product.domain.entities.ProductSelection

@Composable
internal fun ProductView(
    product: Product?,
    selection: ProductSelection,
    expandedDetail: Int?,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: Throwable?,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?,
    onShare: () -> Unit,
    onSelectShade: (String) -> Unit,
    onSelectHeight: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onOpenSizeChart: () -> Unit,
    onToggleDetail: (Int) -> Unit,
    onAddToCart: () -> Unit,
) {
    UggPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        // The photo runs under the status bar; the spinner comes out below it.
        indicatorPadding = WindowInsets.statusBars.asPaddingValues(),
    ) {
        when {
            product != null -> ProductContent(
                product = product,
                selection = selection,
                expandedDetail = expandedDetail,
                contentPadding = contentPadding,
                onBack = onBack,
                onShare = onShare,
                onSelectShade = onSelectShade,
                onSelectHeight = onSelectHeight,
                onSelectSize = onSelectSize,
                onOpenSizeChart = onOpenSizeChart,
                onToggleDetail = onToggleDetail,
                onAddToCart = onAddToCart,
            )

            error != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = UggTheme.spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.m, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = error.message ?: "Не удалось загрузить",
                    style = UggTheme.typography.body,
                    color = UggTheme.colors.contentSecondary,
                    textAlign = TextAlign.Center,
                )
                UggButton(text = "Повторить", onClick = onRetry, style = UggButtonStyle.Secondary, showArrow = false)
            }

            isLoading -> Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UggTheme.colors.accent)
            }
        }
    }
}

@Composable
private fun ProductContent(
    product: Product,
    selection: ProductSelection,
    expandedDetail: Int?,
    contentPadding: PaddingValues,
    onBack: (() -> Unit)?,
    onShare: () -> Unit,
    onSelectShade: (String) -> Unit,
    onSelectHeight: (String) -> Unit,
    onSelectSize: (String) -> Unit,
    onOpenSizeChart: () -> Unit,
    onToggleDetail: (Int) -> Unit,
    onAddToCart: () -> Unit,
) {
    val pagerState = rememberPagerState { product.photoCount.coerceAtLeast(1) }
    val shade = product.shades.firstOrNull { it.id == selection.shadeId }

    // The top inset is deliberately ignored: the gallery runs under the status bar.
    Column(modifier = Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "gallery") {
                ProductGallery(
                    pagerState = pagerState,
                    tint = shade?.color,
                    onBack = onBack,
                    onShare = onShare,
                )
            }
            item(key = "thumbnails") {
                ProductThumbnails(photoCount = product.photoCount, pagerState = pagerState, tint = shade?.color)
            }
            item(key = "summary") { ProductSummary(product) }
            item(key = "options") {
                ProductOptions(
                    product = product,
                    selection = selection,
                    onSelectShade = onSelectShade,
                    onSelectHeight = onSelectHeight,
                    onSelectSize = onSelectSize,
                    onOpenSizeChart = onOpenSizeChart,
                )
            }
            if (product.features.isNotEmpty()) {
                item(key = "features") { ProductFeatures(product.features) }
            }
            if (product.details.isNotEmpty()) {
                item(key = "details") {
                    ProductDetails(details = product.details, expanded = expandedDetail, onToggle = onToggleDetail)
                }
            }
        }

        ProductBottomBar(
            price = product.price,
            summary = selectionSummary(product, selection),
            isComplete = selection.isComplete,
            onAddToCart = onAddToCart,
        )
    }
}

@Composable
private fun ProductBottomBar(
    price: String,
    summary: String,
    isComplete: Boolean,
    onAddToCart: () -> Unit,
) {
    val colors = UggTheme.colors

    Column(modifier = Modifier.fillMaxWidth().background(colors.surfaceElevated)) {
        HorizontalDivider(thickness = 1.dp, color = colors.outline)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = price, style = UggTheme.typography.price, color = colors.content)
                Text(text = summary, style = UggTheme.typography.caption, color = colors.contentSecondary)
            }

            UggButton(
                text = if (isComplete) "В корзину" else "Выберите размер",
                onClick = onAddToCart,
                enabled = isComplete,
                showArrow = isComplete,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** «Каштан · 38» under the price; just the color until a size is picked. */
private fun selectionSummary(product: Product, selection: ProductSelection): String = listOfNotNull(
    product.shades.firstOrNull { it.id == selection.shadeId }?.name,
    selection.size,
).joinToString(" · ")
