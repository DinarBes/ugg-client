package com.ugg.client.shared

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.session.AccessService
import com.ugg.client.kit.auth.presentation.external.authModule
import com.ugg.client.kit.cart.presentation.external.cartModule
import com.ugg.client.kit.core.data.coreDataModule
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.core.domain.AppInfo
import com.ugg.client.kit.core.domain.resolve
import com.ugg.client.kit.home.presentation.external.homeModule
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.navigation.presentation.external.navigationModule
import com.ugg.client.kit.onboarding.presentation.external.onboardingModule
import com.ugg.client.kit.product.presentation.external.productModule
import com.ugg.client.kit.profile.presentation.external.profileModule
import com.ugg.client.kit.stores.presentation.external.storesModule
import com.ugg.client.kit.care.presentation.external.careModule
import com.ugg.client.kit.story.presentation.external.storyModule
import com.ugg.client.kit.splash.presentation.external.splashModule
import com.ugg.client.shared.dsl.TemplateAppBuilder
import io.ktor.client.plugins.auth.providers.BearerTokens
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class TemplateApp(block: TemplateAppBuilder.() -> Unit = {}) {
    val di: DI

    init {
        val builder = TemplateAppBuilder()
        builder.block()

        di = DI {
            bind<TemplateCore>() with instance(builder.core)
            bind<AppEnvironment>() with instance(builder.environment)
            bind<AppInfo>() with instance(builder.appInfo)
            bind<Keystore>() with instance(builder.keystore)

            bind<AccessService>() with singleton { AccessService(instance()) }

            // The composition root is where the keystore-backed tokens meet the API layer: the
            // bearer block reads AccessService and hands tokens to TemplateAuth, so neither
            // api-core nor api-auth ever depends on the keystore.
            bind<TemplateAuth>() with singleton {
                val access: AccessService = instance()

                TemplateAuth(instance()) {
                    bearerAuthConfig {
                        loadTokens {
                            // Absent or unreadable both mean "no Authorization header this time"
                            // — a 401 instead of an abort() on a locked-screen background launch.
                            val accessToken = access.accessTokenOrNull() ?: return@loadTokens null
                            val refreshToken = access.refreshTokenOrNull() ?: return@loadTokens null

                            BearerTokens(accessToken, refreshToken)
                        }

                        refreshTokens {
                            // TODO: call the refresh endpoint when the backend is ready, persist the
                            // new tokens via access.store(...), and return them. On failure clear
                            // them with access.clear() and return null.
                            null
                        }
                    }

                    // The write side: login persists tokens, logout drops them. Both map onto the
                    // same keystore-backed AccessService the read side above reads from.
                    storeTokens { tokens -> access.store(tokens.accessToken, tokens.refreshToken) }
                    clearTokens { access.clear() }
                }
            }

            import(coreDataModule)
            import(authModule)
            import(navigationModule)
            import(splashModule)
            import(onboardingModule)
            import(homeModule)
            import(productModule)
            import(storyModule)
            import(careModule)
            import(storesModule)
            import(cartModule)
            import(profileModule)
        }
    }

    @Suppress("unused")
    val navigationContext: NavigationContext
        get() = di.resolve()
}
