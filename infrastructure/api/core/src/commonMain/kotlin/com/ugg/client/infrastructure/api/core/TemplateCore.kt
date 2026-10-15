package com.ugg.client.infrastructure.api.core

import com.ugg.client.infrastructure.api.core.ktor.ktorModule
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.LazyDelegate
import org.kodein.di.bind
import org.kodein.di.instance

class TemplateCore(block: TemplateCoreConfiguration.() -> Unit) {
    val di: DI

    init {
        val configuration = TemplateCoreConfiguration()
        configuration.block()

        di = DI {
            bind<TemplateCoreConfiguration>() with instance(configuration)
            import(ktorModule)
        }
    }

    val client: LazyDelegate<HttpClient>
        get() = di.instance()
}
