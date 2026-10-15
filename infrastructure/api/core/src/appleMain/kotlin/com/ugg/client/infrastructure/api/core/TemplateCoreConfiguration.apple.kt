package com.ugg.client.infrastructure.api.core

import io.ktor.client.plugins.logging.LogLevel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateCoreConfiguration {
    actual var api: TemplateApi = TemplateApi.Client.Localhost
    actual var logLevel: LogLevel = LogLevel.INFO
}
