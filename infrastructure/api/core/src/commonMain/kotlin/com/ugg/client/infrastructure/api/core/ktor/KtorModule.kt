package com.ugg.client.infrastructure.api.core.ktor

import cloud.paybuddy.multiplatform.foundation.core.MultiplatformCore
import cloud.paybuddy.multiplatform.foundation.network.createClient
import com.ugg.client.infrastructure.api.core.TemplateCoreConfiguration
import com.ugg.client.infrastructure.api.core.ktor.plugins.templateDefaultRequest
import com.ugg.client.infrastructure.api.core.ktor.plugins.templateResponseValidation
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

internal val ktorModule: DI.Module
    get() = DI.Module("ktor-module") {
        bind<HttpClient>() with provider {
            val core: MultiplatformCore = instance()
            val configuration: TemplateCoreConfiguration = instance()

            core.createClient {
                templateDefaultRequest(configuration.api)
                templateResponseValidation()
            }
        }
    }
