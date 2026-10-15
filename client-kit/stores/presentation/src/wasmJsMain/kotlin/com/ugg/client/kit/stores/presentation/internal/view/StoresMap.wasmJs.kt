package com.ugg.client.kit.stores.presentation.internal.view

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.ugg.client.kit.stores.domain.entities.Store

/**
 * Yandex MapKit has no web SDK for Compose, so the web shows the placeholder; a tap opens Yandex
 * Maps in a new tab with a marker on every store (or on the selected one).
 */
@Composable
internal actual fun StoresMap(
    stores: List<Store>,
    selectedStoreId: String?,
    onStoreTap: (String) -> Unit,
    modifier: Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val shown = stores.filter { it.id == selectedStoreId }.ifEmpty { stores }

    StoresMapPlaceholder(
        message = "Открыть на Яндекс Картах",
        modifier = if (shown.isEmpty()) modifier else modifier.clickable { uriHandler.openUri(yandexMapsUrl(shown)) },
    )
}

/** `pt` takes `lng,lat` pairs separated by `~`; one store is zoomed in, several fit the view. */
private fun yandexMapsUrl(stores: List<Store>): String {
    val points = stores.joinToString("~") { "${it.longitude},${it.latitude}" }
    val center = stores.first()
    val zoom = if (stores.size == 1) 16 else 4
    return "https://yandex.ru/maps/?pt=$points&ll=${center.longitude},${center.latitude}&z=$zoom&l=map"
}
