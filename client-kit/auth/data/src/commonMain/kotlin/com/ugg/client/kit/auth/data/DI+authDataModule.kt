package com.ugg.client.kit.auth.data

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.auth.data.repositories.MockAuthRepository
import com.ugg.client.kit.auth.data.repositories.SharedAuthRepository
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.core.data.scopes.TemplateCoreScope
import com.ugg.client.kit.core.domain.AppEnvironment
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.scoped
import org.kodein.di.singleton

val authDataModule get() = DI.Module("auth-data") {
    // PRODUCTION signs in with Firebase (PhoneAuthService, scoped in core/data). MOCK keeps the
    // old seam: TemplateAuth, bound at the composition root, plus the fixed code 123456.
    bind<AuthUseCase>() with factory {
        val core: TemplateCore = instance()

        when (instance<AppEnvironment>()) {
            AppEnvironment.PRODUCTION -> SharedAuthRepository(on(core).instance())
            AppEnvironment.MOCK -> MockAuthRepository(instance())
        }
    }
}