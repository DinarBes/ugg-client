package com.ugg.client.infrastructure.api.core

import cloud.paybuddy.multiplatform.foundation.core.dsl.MultiplatformCoreBuilder
import io.ktor.client.plugins.logging.LogLevel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TemplateCoreConfiguration() {
    var api: TemplateApi
    var logLevel: LogLevel
    fun MultiplatformCoreBuilder.multiplatformSetup()
}
