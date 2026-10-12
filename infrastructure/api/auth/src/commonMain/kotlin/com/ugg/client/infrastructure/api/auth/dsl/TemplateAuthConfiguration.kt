package com.ugg.client.infrastructure.api.auth.dsl

import com.ugg.client.infrastructure.api.auth.TemplateTokens
import io.ktor.client.plugins.auth.providers.BearerAuthConfig

/**
 * Configures how the auth HTTP client reads and writes bearer tokens.
 *
 * The API layer deliberately knows nothing about *where* tokens live — the keystore, a refresh
 * endpoint, an in-memory fake in tests. The composition root supplies the blocks, and they are the
 * only channel through which tokens enter or leave the client. This is what keeps
 * `infrastructure:api` free of any dependency on the token store.
 *
 * - [bearerAuthConfig] — the read side: Ktor pulls tokens for outgoing requests.
 * - [storeTokens] / [clearTokens] — the write side: the app pushes tokens after login and drops
 *   them on logout. Ktor never calls these; [TemplateAuth.store] / [TemplateAuth.clearTokens] do.
 */
class TemplateAuthConfiguration {
    internal var bearerAuthConfigBlock: BearerAuthConfig.() -> Unit = {}
    internal var storeTokensBlock: (TemplateTokens) -> Unit = {}
    internal var clearTokensBlock: () -> Unit = {}

    fun bearerAuthConfig(block: BearerAuthConfig.() -> Unit) {
        bearerAuthConfigBlock = block
    }

    fun storeTokens(block: (TemplateTokens) -> Unit) {
        storeTokensBlock = block
    }

    fun clearTokens(block: () -> Unit) {
        clearTokensBlock = block
    }
}
