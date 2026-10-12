package com.ugg.client.infrastructure.api.core.ktor.plugins

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.http.isSuccess

internal fun HttpClientConfig<*>.templateResponseValidation() {
    install(HttpTimeout) {
        requestTimeoutMillis = 40_000
    }

    expectSuccess = true

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val throwable = (cause as? ResponseException) ?: return@handleResponseExceptionWithRequest
            val response = throwable.response

            if (response.status.isSuccess()) return@handleResponseExceptionWithRequest

            val result = runCatching { response.body<Exception>() }
            throw result.getOrNull() ?: Exception(throwable)
        }
    }
}
