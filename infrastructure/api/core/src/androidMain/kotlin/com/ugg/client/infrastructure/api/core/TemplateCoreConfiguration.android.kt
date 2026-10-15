package com.ugg.client.infrastructure.api.core

import android.content.Context
import io.ktor.client.plugins.logging.LogLevel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateCoreConfiguration {
    /** Kept for callers that pass it; the plain Ktor client (OkHttp) needs no Context. */
    var androidContext: Context? = null
    actual var api: TemplateApi = TemplateApi.Client.Localhost
    actual var logLevel: LogLevel = LogLevel.INFO
}
