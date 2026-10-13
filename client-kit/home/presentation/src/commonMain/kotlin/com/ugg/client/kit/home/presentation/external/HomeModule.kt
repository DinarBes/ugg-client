package com.ugg.client.kit.home.presentation.external

import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.home.data.useCases.MockHomeUseCase
import com.ugg.client.kit.home.domain.useCases.HomeUseCase
import com.ugg.client.kit.home.presentation.external.view.models.HomeViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val homeModule: DI.Module
    get() = DI.Module("home.module") {
        bind<HomeViewModel>() with factory {
            HomeViewModel(instance())
        }

        bind<HomeUseCase>() with factory {
            // PRODUCTION gets its own implementation once the content moves to Firebase.
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockHomeUseCase()
                AppEnvironment.MOCK -> MockHomeUseCase()
            }
        }
    }
