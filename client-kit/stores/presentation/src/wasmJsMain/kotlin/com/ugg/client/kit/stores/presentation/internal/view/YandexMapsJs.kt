package com.ugg.client.kit.stores.presentation.internal.view

import kotlin.js.Promise

// Thin bridge to Yandex Maps JS API v3 (`ymaps3`), loaded at runtime by a <script> tag, so
// there is nothing to bundle and no external declarations to keep in sync with its typings.

/**
 * Adds the API script once per page and waits for `ymaps3.ready`. Resolves to `true` when the
 * API is usable, `false` when it failed (no network, bad key, the daily limit is over); a failed
 * load is retried on the next call.
 */
internal fun loadYandexMaps(apiKey: String): Promise<JsAny?> = js(
    """(() => {
        if (!window.__uggYmaps) {
            window.__uggYmaps = new Promise((resolve, reject) => {
                const script = document.createElement('script');
                script.src = 'https://api-maps.yandex.ru/v3/?apikey=' + encodeURIComponent(apiKey) + '&lang=ru_RU';
                script.onload = () => window.ymaps3 ? window.ymaps3.ready.then(resolve, reject) : reject(new Error('ymaps3 is missing'));
                script.onerror = () => reject(new Error('Yandex Maps script failed to load'));
                document.head.appendChild(script);
            });
            window.__uggYmaps.catch((e) => { console.warn('Yandex Maps:', e); window.__uggYmaps = null; });
        }
        return window.__uggYmaps.then(() => true, () => false);
    })()""",
)

internal fun createYandexMap(element: JsAny, longitude: Double, latitude: Double, zoom: Double): JsAny = js(
    """(() => {
        const map = new ymaps3.YMap(element, { location: { center: [longitude, latitude], zoom: zoom } });
        map.addChild(new ymaps3.YMapDefaultSchemeLayer({}));
        map.addChild(new ymaps3.YMapDefaultFeaturesLayer({}));
        map.__uggMarkers = [];
        return map;
    })()""",
)

internal fun moveYandexMap(map: JsAny, longitude: Double, latitude: Double, zoom: Double, durationMs: Int): Unit = js(
    "map.setLocation({ center: [longitude, latitude], zoom: zoom, duration: durationMs })",
)

/**
 * Replaces the markers. [markersJson]: `[{"id", "lat", "lng", "selected"}]`. Markers are round,
 * in the brand colors, like the ones the Android map draws.
 */
internal fun setYandexMarkers(
    map: JsAny,
    markersJson: String,
    onTap: (String) -> Unit,
    fill: String,
    selectedFill: String,
    stroke: String,
): Unit = js(
    """(() => {
        for (const marker of map.__uggMarkers) map.removeChild(marker);
        map.__uggMarkers = [];
        for (const point of JSON.parse(markersJson)) {
            const size = point.selected ? 38 : 30;
            const element = document.createElement('div');
            element.style.cssText = 'width:' + size + 'px;height:' + size + 'px;border-radius:50%;box-sizing:border-box;'
                + 'border:3px solid ' + stroke + ';background:' + (point.selected ? selectedFill : fill) + ';'
                + 'transform:translate(-50%,-50%);cursor:pointer;display:flex;align-items:center;justify-content:center;'
                + 'box-shadow:0 2px 6px rgba(0,0,0,0.25)';
            const dot = document.createElement('div');
            dot.style.cssText = 'width:' + (size / 4) + 'px;height:' + (size / 4) + 'px;border-radius:50%;background:' + stroke;
            element.appendChild(dot);
            element.addEventListener('click', (event) => { event.stopPropagation(); onTap(point.id); });
            const marker = new ymaps3.YMapMarker({ coordinates: [point.lng, point.lat], zIndex: point.selected ? 10 : 0 }, element);
            map.addChild(marker);
            map.__uggMarkers.push(marker);
        }
    })()""",
)

internal fun destroyYandexMap(map: JsAny): Unit = js("map.destroy()")
