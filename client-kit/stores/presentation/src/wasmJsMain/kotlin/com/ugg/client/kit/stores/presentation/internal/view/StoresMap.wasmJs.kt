package com.ugg.client.kit.stores.presentation.internal.view

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.WebElementView
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.stores.domain.entities.Store
import com.ugg.client.kit.stores.presentation.external.maps.StoresMapWeb
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.w3c.dom.HTMLDivElement
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

private const val SINGLE_STORE_ZOOM = 14.0

/** Matches the rounded corners the Compose shapes give the map on Android. */
private const val CORNER_RADIUS_PX = 24

private enum class MapStatus { Loading, Ready, Failed }

/**
 * Yandex Maps JS API v3 inside the Compose page (an HTML element over the canvas). Until the
 * API is loaded — and for good if it cannot be (no key, no network, the daily limit is over) —
 * the placeholder links to Yandex Maps instead.
 */
@Composable
internal actual fun StoresMap(
    stores: List<Store>,
    selectedStoreId: String?,
    onStoreTap: (String) -> Unit,
    modifier: Modifier,
) {
    val apiKey = StoresMapWeb.apiKey
    var status by remember(apiKey) { mutableStateOf(if (apiKey.isBlank()) MapStatus.Failed else MapStatus.Loading) }

    LaunchedEffect(apiKey) {
        if (apiKey.isBlank()) return@LaunchedEffect
        val isLoaded = loadYandexMaps(apiKey).await<JsBoolean>().toBoolean()
        status = if (isLoaded) MapStatus.Ready else MapStatus.Failed
    }

    when (status) {
        MapStatus.Ready -> YandexMap(stores, selectedStoreId, onStoreTap, modifier)
        MapStatus.Loading -> StoresMapPlaceholder(message = "Загружаем карту…", modifier = modifier)
        MapStatus.Failed -> YandexMapsLink(stores, selectedStoreId, modifier)
    }
}

// WebElementView is still marked experimental in some Compose Multiplatform releases.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun YandexMap(
    stores: List<Store>,
    selectedStoreId: String?,
    onStoreTap: (String) -> Unit,
    modifier: Modifier,
) {
    val colors = UggTheme.colors
    val currentOnStoreTap by rememberUpdatedState(onStoreTap)

    var element by remember { mutableStateOf<HTMLDivElement?>(null) }
    var map by remember { mutableStateOf<JsAny?>(null) }

    WebElementView(
        factory = {
            document.createElement("div").unsafeCast<HTMLDivElement>().apply {
                // A plain style attribute: the kotlinx-browser CSS bindings lack some properties.
                setAttribute("style", "width:100%;height:100%;border-radius:${CORNER_RADIUS_PX}px;overflow:hidden")
            }.also { element = it }
        },
        modifier = modifier,
    )

    // Created once the element is laid out, framing every store.
    LaunchedEffect(element) {
        val container = element ?: return@LaunchedEffect
        withFrameNanos { }
        val (longitude, latitude, zoom) = frame(stores) ?: Triple(104.28, 52.29, 10.0)
        map = createYandexMap(container, longitude, latitude, zoom)
    }

    DisposableEffect(map) {
        val created = map
        onDispose { created?.let(::destroyYandexMap) }
    }

    // Re-frame when the set of stores changes (the city filter).
    LaunchedEffect(map, stores) {
        val current = map ?: return@LaunchedEffect
        val (longitude, latitude, zoom) = frame(stores) ?: return@LaunchedEffect
        moveYandexMap(current, longitude, latitude, zoom, durationMs = 0)
    }

    // Glide to the store picked on the map or in the list.
    LaunchedEffect(map, selectedStoreId) {
        val current = map ?: return@LaunchedEffect
        val store = stores.firstOrNull { it.id == selectedStoreId } ?: return@LaunchedEffect
        moveYandexMap(current, store.longitude, store.latitude, SINGLE_STORE_ZOOM - 1, durationMs = 400)
    }

    LaunchedEffect(map, stores, selectedStoreId, colors) {
        val current = map ?: return@LaunchedEffect
        val markers = JsonArray(
            stores.map { store ->
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(store.id),
                        "lat" to JsonPrimitive(store.latitude),
                        "lng" to JsonPrimitive(store.longitude),
                        "selected" to JsonPrimitive(store.id == selectedStoreId),
                    ),
                )
            },
        )
        setYandexMarkers(
            map = current,
            markersJson = markers.toString(),
            onTap = { id -> currentOnStoreTap(id) },
            fill = colors.accent.css(),
            selectedFill = colors.surfaceInverse.css(),
            stroke = colors.surfaceElevated.css(),
        )
    }
}

/** The map could not load: the placeholder opens Yandex Maps with every store (or the selected one). */
@Composable
private fun YandexMapsLink(stores: List<Store>, selectedStoreId: String?, modifier: Modifier) {
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

/** Center (lng, lat) and zoom that fit every store — the same formula as on Android. */
private fun frame(stores: List<Store>): Triple<Double, Double, Double>? {
    if (stores.isEmpty()) return null

    val minLat = stores.minOf { it.latitude }
    val maxLat = stores.maxOf { it.latitude }
    val minLon = stores.minOf { it.longitude }
    val maxLon = stores.maxOf { it.longitude }

    val span = max(maxLat - minLat, maxLon - minLon)
    val zoom = if (span < 1e-4) SINGLE_STORE_ZOOM else (ln(360.0 / span) / ln(2.0) - 1.3).coerceIn(3.0, 16.0)

    return Triple((minLon + maxLon) / 2, (minLat + maxLat) / 2, zoom)
}

private fun Color.css(): String =
    "rgba(${(red * 255).roundToInt()},${(green * 255).roundToInt()},${(blue * 255).roundToInt()},$alpha)"
