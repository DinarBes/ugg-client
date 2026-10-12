package com.ugg.client.infrastructure.api.auth

import com.ugg.client.infrastructure.api.auth.context.AuthContext
import com.ugg.client.infrastructure.api.auth.dsl.TemplateAuthConfiguration
import com.ugg.client.infrastructure.api.auth.ktor.ktorModule
import com.ugg.client.infrastructure.api.core.TemplateCore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.kodein.di.DI
import org.kodein.di.LazyDelegate
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * Bearer-authenticated HTTP client, layered over [TemplateCore].
 *
 * Token loading is provided by the caller through [configuration]'s `bearerAuthConfig` block, so
 * this module never touches the keystore. Construct it with the DSL overload:
 *
 * ```
 * TemplateAuth(core) {
 *     bearerAuthConfig {
 *         loadTokens { … }
 *         refreshTokens { … }
 *     }
 * }
 * ```
 */
class TemplateAuth(val core: TemplateCore, val configuration: TemplateAuthConfiguration) {
    val di: DI = DI {
        extend(core.di, allowOverride = true)

        bind<AuthContext>() with singleton { AuthContext() }
        bind<TemplateAuthConfiguration>() with instance(configuration)

        import(ktorModule, allowOverride = true)
    }

    val context: LazyDelegate<AuthContext>
        get() = di.instance()

    val client: LazyDelegate<HttpClient>
        get() = di.instance()

    /**
     * Persists [tokens] through the configured `storeTokens` block, then invalidates the client's
     * cached token so the next request re-runs `loadTokens` and picks up what was just stored.
     */
    fun store(tokens: TemplateTokens) {
        configuration.storeTokensBlock(tokens)
        invalidateClientCache()
    }

    /**
     * Drops the persisted tokens through the configured `clearTokens` block and invalidates the
     * client's cached token. After this the next request runs `loadTokens`, finds nothing, and
     * goes out unauthenticated.
     */
    fun clearTokens() {
        configuration.clearTokensBlock()
        invalidateClientCache()
    }

    private fun invalidateClientCache() {
        val httpClient: HttpClient = di.direct.instance()
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}

fun TemplateAuth(core: TemplateCore, block: TemplateAuthConfiguration.() -> Unit = {}) =
    TemplateAuth(core, TemplateAuthConfiguration().apply(block))
