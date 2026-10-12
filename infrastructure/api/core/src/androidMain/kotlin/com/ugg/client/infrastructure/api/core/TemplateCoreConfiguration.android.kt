package com.ugg.client.infrastructure.api.core

import android.content.Context
import cloud.paybuddy.multiplatform.foundation.core.context.AndroidContextPlugin
import cloud.paybuddy.multiplatform.foundation.core.dsl.MultiplatformCoreBuilder
import io.ktor.client.plugins.logging.LogLevel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateCoreConfiguration {
    var androidContext: Context? = null
    actual var api: TemplateApi = TemplateApi.Client.Localhost
    actual var logLevel: LogLevel = LogLevel.INFO

    actual fun MultiplatformCoreBuilder.multiplatformSetup() {
        val ctx = androidContext
        if (ctx != null)
            install(AndroidContextPlugin) { context = ctx }
    }
}
