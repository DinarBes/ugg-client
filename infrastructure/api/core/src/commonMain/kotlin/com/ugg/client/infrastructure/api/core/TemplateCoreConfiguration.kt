package com.ugg.client.infrastructure.api.core

import io.ktor.client.plugins.logging.LogLevel

/** Settings of the HTTP client behind [TemplateCore]. Platforms may add their own fields. */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TemplateCoreConfiguration() {
    var api: TemplateApi
    var logLevel: LogLevel
}
