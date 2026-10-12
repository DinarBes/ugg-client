package com.ugg.client.infrastructure.api.core

import io.ktor.http.URLProtocol

sealed class TemplateApi {
    abstract val protocol: URLProtocol
    abstract val host: String
    abstract val path: List<String>

    sealed class Client : TemplateApi() {
        data object Localhost : Client() {
            override val protocol = URLProtocol.HTTP
            override val host = "localhost:8080"
            override val path = listOf("")
        }
    }
}
