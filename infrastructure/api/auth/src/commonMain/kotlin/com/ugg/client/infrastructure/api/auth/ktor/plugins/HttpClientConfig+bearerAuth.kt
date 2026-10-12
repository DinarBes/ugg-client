package com.ugg.client.infrastructure.api.auth.ktor.plugins

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthConfig
import io.ktor.client.plugins.auth.providers.bearer

/**
 * Installs bearer auth, delegating token loading to the caller-supplied [block].
 *
 * The block comes from [com.ugg.client.infrastructure.api.auth.dsl.TemplateAuthConfiguration],
 * which is how tokens reach the client without this module depending on where they are stored.
 */
internal fun HttpClientConfig<*>.templateBearerAuth(
    block: BearerAuthConfig.() -> Unit,
) {
    install(Auth) {
        bearer(block)
    }
}
