package com.ugg.client.android

import android.app.Application
import com.ugg.client.kit.stores.presentation.external.maps.StoresMapKit

class UggApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        StoresMapKit.initialize(this, BuildConfig.YANDEX_API_KEY)
    }
}
