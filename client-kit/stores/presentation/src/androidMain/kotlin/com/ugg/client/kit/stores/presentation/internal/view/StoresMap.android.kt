package com.ugg.client.kit.stores.presentation.internal.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ugg.client.kit.core.presentation.external.theme.UggTheme
import com.ugg.client.kit.stores.domain.entities.Store
import com.ugg.client.kit.stores.presentation.external.maps.StoresMapKit
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlin.math.ln
import kotlin.math.max

private const val SINGLE_STORE_ZOOM = 14f

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun StoresMap(
    stores: List<Store>,
    selectedStoreId: String?,
    onStoreTap: (String) -> Unit,
    modifier: Modifier,
) {
    if (!StoresMapKit.isInitialized) {
        StoresMapPlaceholder(message = "Карта недоступна: не задан ключ Яндекс Карт", modifier = modifier)
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current.density
    val colors = UggTheme.colors
    val shape = UggTheme.shapes.large

    val mapView = remember { MapView(context) }
    val currentOnStoreTap by rememberUpdatedState(onStoreTap)
    // MapKit keeps tap listeners weakly: without a strong reference here they stop firing.
    val tapListeners = remember { mutableListOf<MapObjectTapListener>() }

    val markerIcon = remember(colors) {
        ImageProvider.fromBitmap(markerBitmap(density, 30f, colors.accent.toArgb(), colors.surfaceElevated.toArgb()))
    }
    val selectedMarkerIcon = remember(colors) {
        ImageProvider.fromBitmap(markerBitmap(density, 38f, colors.surfaceInverse.toArgb(), colors.surfaceElevated.toArgb()))
    }

    MapLifecycle(mapView)

    // Frame every store whenever the set changes (first load, city filter).
    LaunchedEffect(mapView, stores) {
        frame(mapView, stores)?.let { mapView.mapWindow.map.move(it) }
    }

    // Glide to the store picked on the map or in the list.
    LaunchedEffect(mapView, selectedStoreId) {
        val store = stores.firstOrNull { it.id == selectedStoreId } ?: return@LaunchedEffect
        val zoom = mapView.mapWindow.map.cameraPosition.zoom.coerceAtLeast(SINGLE_STORE_ZOOM - 2)

        mapView.mapWindow.map.move(
            CameraPosition(Point(store.latitude, store.longitude), zoom, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 0.4f),
            null,
        )
    }

    LaunchedEffect(mapView, stores, selectedStoreId, markerIcon) {
        val mapObjects = mapView.mapWindow.map.mapObjects
        mapObjects.clear()
        tapListeners.clear()

        stores.forEach { store ->
            val listener = MapObjectTapListener { _, _ ->
                currentOnStoreTap(store.id)
                true
            }
            tapListeners += listener

            mapObjects.addPlacemark().apply {
                geometry = Point(store.latitude, store.longitude)
                setIcon(if (store.id == selectedStoreId) selectedMarkerIcon else markerIcon)
                addTapListener(listener)
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .clip(shape)
            // The map lives inside a scrolling list: while a finger is on it, the list must not
            // steal the gesture, or panning the map scrolls the screen instead.
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                        mapView.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        mapView.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            },
    )
}

/** MapKit renders only between `onStart` and `onStop` of both the factory and the view. */
@Composable
private fun MapLifecycle(mapView: MapView) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, mapView) {
        var isStarted = false

        fun start() {
            if (isStarted) return
            MapKitFactory.getInstance().onStart()
            mapView.onStart()
            isStarted = true
        }

        fun stop() {
            if (!isStarted) return
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
            isStarted = false
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_STOP -> stop()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stop()
        }
    }
}

/** Camera that fits every store, or `null` when there is nothing to show. */
private fun frame(mapView: MapView, stores: List<Store>): CameraPosition? {
    if (stores.isEmpty()) return null

    val minLat = stores.minOf { it.latitude }
    val maxLat = stores.maxOf { it.latitude }
    val minLon = stores.minOf { it.longitude }
    val maxLon = stores.maxOf { it.longitude }
    val center = Point((minLat + maxLat) / 2, (minLon + maxLon) / 2)

    val span = max(maxLat - minLat, maxLon - minLon)
    // Each zoom level halves the visible degrees; the margin keeps markers off the edges.
    val zoom = if (span < 1e-4) SINGLE_STORE_ZOOM else (ln(360.0 / span) / ln(2.0) - 1.3).toFloat().coerceIn(3f, 16f)

    return CameraPosition(center, zoom, 0f, 0f)
}

/** Round marker in brand colors, drawn in code so no drawable resource is needed. */
private fun markerBitmap(density: Float, sizeDp: Float, fill: Int, stroke: Int): Bitmap {
    val size = (sizeDp * density).toInt()
    val strokeWidth = 3f * density
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = size / 2f

    canvas.drawCircle(radius, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = stroke })
    canvas.drawCircle(radius, radius, radius - strokeWidth, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
    canvas.drawCircle(radius, radius, radius / 4f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = stroke })

    return bitmap
}
