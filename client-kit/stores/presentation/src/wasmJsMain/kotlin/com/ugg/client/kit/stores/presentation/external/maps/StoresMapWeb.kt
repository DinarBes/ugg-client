package com.ugg.client.kit.stores.presentation.external.maps

/**
 * Yandex Maps JS API v3 setup for the web client. Call once from `main()` with the
 * «JavaScript API» key (not the MapKit one), before the stores screen is shown.
 *
 * The key is public by nature — it ships in the page — and is protected by the «HTTP Referer»
 * restriction in the Yandex developer console. Without a key the stores screen shows a link to
 * Yandex Maps instead of the map.
 */
object StoresMapWeb {
    var apiKey: String = ""
        private set

    fun initialize(apiKey: String) {
        this.apiKey = apiKey.trim()
    }
}
