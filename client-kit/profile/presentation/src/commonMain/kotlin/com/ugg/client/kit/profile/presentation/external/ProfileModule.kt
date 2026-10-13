package com.ugg.client.kit.profile.presentation.external

import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.profile.data.useCases.MockProfileUseCase
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import com.ugg.client.kit.profile.presentation.external.view.models.ProfileViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val profileModule: DI.Module
    get() = DI.Module("profile.module") {
        bind<ProfileViewModel>() with factory {
            ProfileViewModel(instance())
        }

        bind<ProfileUseCase>() with factory {
            // PRODUCTION gets its own implementation once accounts and orders come from the backend.
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockProfileUseCase()
                AppEnvironment.MOCK -> MockProfileUseCase()
            }
        }
    }
