package com.ugg.client.infrastructure.api.core.ktor

import com.ugg.client.infrastructure.api.core.TemplateCoreConfiguration
import com.ugg.client.infrastructure.api.core.ktor.plugins.templateDefaultRequest
import com.ugg.client.infrastructure.api.core.ktor.plugins.templateResponseValidation
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

internal val ktorModule: DI.Module
    get() = DI.Module("ktor-module") {
        bind<HttpClient>() with provider {
            val configuration: TemplateCoreConfiguration = instance()

            // No engine argument: each platform has exactly one engine on its classpath
            // (OkHttp, Darwin, Js), and Ktor takes that one.
            HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                install(Logging) {
                    level = configuration.logLevel
                }

                templateDefaultRequest(configuration.api)
                templateResponseValidation()
            }
        }
    }
