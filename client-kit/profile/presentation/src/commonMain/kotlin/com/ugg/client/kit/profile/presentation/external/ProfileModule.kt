package com.ugg.client.kit.profile.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.profile.data.useCases.MockProfileUseCase
import com.ugg.client.kit.profile.data.useCases.SharedProfileUseCase
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import com.ugg.client.kit.profile.presentation.external.view.models.ProfileViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

val profileModule: DI.Module
    get() = DI.Module("profile.module") {
        bind<ProfileViewModel>() with factory {
            ProfileViewModel(instance())
        }

        bind<ProfileUseCase>() with factory {
            // PRODUCTION: Firebase Auth + Firestore (scoped in core/data). MOCK: the stub profile.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedProfileUseCase(
                    auth = on(core).instance(),
                    users = on(core).instance(),
                    orders = on(core).instance(),
                )
                AppEnvironment.MOCK -> MockProfileUseCase()
            }
        }
    }
