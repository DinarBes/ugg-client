package com.ugg.client.kit.stores.presentation.internal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ugg.client.kit.core.presentation.external.components.UggButton
import com.ugg.client.kit.core.presentation.external.components.UggButtonStyle
import com.ugg.client.kit.core.presentation.external.components.UggPhotoPlaceholder
import com.ugg.client.kit.core.presentation.external.icons.UggIcons
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.stores.domain.entities.Store
import kotlinx.coroutines.launch

/** Items above the first store card: header, map, city filter. */
private const val ITEMS_BEFORE_STORES = 3

@Composable
internal fun StoresView(
    stores: List<Store>,
    cities: List<String>,
    totalCount: Int,
    city: String?,
    selectedStoreId: String?,
    isLoading: Boolean,
    error: Throwable?,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onSelectCity: (String?) -> Unit,
    onSelectStore: (String?) -> Unit,
    onRoute: (Store) -> Unit,
    onCall: (Store) -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val horizontal = UggTheme.spacing.screenHorizontal

    when {
        totalCount > 0 -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item(key = "header") { Header() }

            item(key = "map") {
                StoresMap(
                    stores = stores,
                    selectedStoreId = selectedStoreId,
                    onStoreTap = { id ->
                        onSelectStore(id)
                        // Bring the tapped store's card into view under the map.
                        val index = stores.indexOfFirst { it.id == id }
                        if (index >= 0) scope.launch { listState.animateScrollToItem(ITEMS_BEFORE_STORES + index) }
                    },
                    modifier = Modifier
                        .padding(horizontal = horizontal, vertical = UggTheme.spacing.s)
                        .fillMaxWidth()
                        .height(300.dp),
                )
            }

            item(key = "filter") {
                CityFilter(cities = cities, totalCount = totalCount, city = city, onSelect = onSelectCity)
            }

            items(stores, key = { it.id }) { store ->
                StoreCard(
                    store = store,
                    isSelected = store.id == selectedStoreId,
                    onClick = { onSelectStore(store.id) },
                    onRoute = { onRoute(store) },
                    onCall = { onCall(store) },
                    modifier = Modifier.padding(horizontal = horizontal, vertical = UggTheme.spacing.xs),
                )
            }

            item(key = "bottom") { Spacer(Modifier.height(UggTheme.spacing.l)) }
        }

        error != null -> Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = horizontal),
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

@Composable
private fun Header() {
    Column(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = "Наши точки", style = UggTheme.typography.displayL, color = UggTheme.colors.content)
        Text(
            text = "Примерьте угги вживую и подберите размер вместе с мастером.",
            style = UggTheme.typography.bodySmall,
            color = UggTheme.colors.contentSecondary,
        )
    }
}

@Composable
private fun CityFilter(cities: List<String>, totalCount: Int, city: String?, onSelect: (String?) -> Unit) {
    if (cities.size < 2) return

    Row(
        modifier = Modifier.padding(horizontal = UggTheme.spacing.screenHorizontal, vertical = UggTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
    ) {
        Pill(text = "Все · $totalCount", isSelected = city == null, onClick = { onSelect(null) })
        cities.forEach { name ->
            Pill(text = name, isSelected = city == name, onClick = { onSelect(name) })
        }
    }
}

@Composable
private fun Pill(text: String, isSelected: Boolean, onClick: (() -> Unit)? = null, color: Color? = null, background: Color? = null) {
    val colors = UggTheme.colors

    Text(
        text = text,
        style = UggTheme.typography.caption,
        color = color ?: if (isSelected) colors.contentInverse else colors.content,
        modifier = Modifier
            .clip(UggTheme.shapes.pill)
            .background(background ?: if (isSelected) colors.surfaceInverse else colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = UggTheme.spacing.s, vertical = 6.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoreCard(
    store: Store,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRoute: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.content else colors.outline, shape)
            .clickable(onClick = onClick),
    ) {
        UggPhotoPlaceholder(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = UggTheme.shapes.large.copy(
                bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp),
            ),
        )

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = store.city.uppercase(), style = UggTheme.typography.overline, color = colors.accent)
                Pill(
                    text = store.status,
                    isSelected = false,
                    color = if (store.isOpen) colors.info else colors.contentSecondary,
                    background = if (store.isOpen) colors.info.copy(alpha = 0.14f) else colors.surface,
                )
            }

            Text(text = store.name, style = UggTheme.typography.displayM, color = colors.content)
            InfoRow(icon = UggIcons.Pin, text = store.address)
            InfoRow(icon = UggIcons.Clock, text = store.hours)

            if (store.services.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    store.services.forEach { Pill(text = it, isSelected = false) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = UggTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UggButton(text = "Маршрут", onClick = onRoute, showArrow = false, modifier = Modifier.weight(1f))
                UggButton(
                    text = "Позвонить",
                    onClick = onCall,
                    style = UggButtonStyle.Secondary,
                    showArrow = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(UggTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = UggTheme.colors.contentSecondary, modifier = Modifier.size(16.dp))
        Text(text = text, style = UggTheme.typography.bodySmall, color = UggTheme.colors.contentSecondary)
    }
}
