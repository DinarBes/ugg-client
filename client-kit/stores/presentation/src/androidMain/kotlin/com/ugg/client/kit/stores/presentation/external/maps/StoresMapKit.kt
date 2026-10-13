package com.ugg.client.kit.stores.presentation.external.maps

import android.content.Context
import co.touchlab.kermit.Logger
import com.yandex.mapkit.MapKitFactory

/**
 * One-time Yandex MapKit setup. Call from `Application.onCreate` — MapKit allows `setApiKey`
 * exactly once per process, so an Activity (recreated on rotation) is the wrong place.
 *
 * Without a key the stores screen shows a placeholder instead of the map rather than crashing.
 */
object StoresMapKit {
    var isInitialized: Boolean = false
        private set

    fun initialize(context: Context, apiKey: String) {
        if (isInitialized) return

        if (apiKey.isBlank()) {
            Logger.w { "Yandex MapKit API key is empty — add YANDEX_API_KEY to keys.properties; the map is disabled." }
            return
        }

        MapKitFactory.setApiKey(apiKey)
        MapKitFactory.initialize(context.applicationContext)
        isInitialized = true
    }
}
