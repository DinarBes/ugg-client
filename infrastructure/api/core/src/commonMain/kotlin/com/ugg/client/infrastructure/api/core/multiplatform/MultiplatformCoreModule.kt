package com.ugg.client.infrastructure.api.core.multiplatform

import cloud.paybuddy.multiplatform.foundation.core.MultiplatformCore
import cloud.paybuddy.multiplatform.foundation.network.NetworkPlugin
import cloud.paybuddy.multiplatform.foundation.network.engine.MultiplatformEngine
import cloud.paybuddy.multiplatform.foundation.serialization.SerializationPlugin
import com.ugg.client.infrastructure.api.core.TemplateCoreConfiguration
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

internal val multiplatformCoreModule: DI.Module
    get() = DI.Module("multiplatform-core") {
        bind<MultiplatformCore>() with provider {
            val configuration: TemplateCoreConfiguration = instance()

            MultiplatformCore {
                with(configuration) { multiplatformSetup() }

                // No keystore here: the API layer never touches secure storage. Tokens are
                // supplied to the auth client through TemplateAuth's bearerAuthConfig block,
                // wired at the composition root.
                install(NetworkPlugin) {
                    engine(MultiplatformEngine)

                    standardLogging {
                        level = configuration.logLevel
                    }
                }

                install(SerializationPlugin)
            }
        }
    }
