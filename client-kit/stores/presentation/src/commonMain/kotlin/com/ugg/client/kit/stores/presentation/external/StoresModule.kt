package com.ugg.client.kit.stores.presentation.external

import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.stores.data.useCases.MockStoresUseCase
import com.ugg.client.kit.stores.domain.useCases.StoresUseCase
import com.ugg.client.kit.stores.presentation.external.view.models.StoresViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val storesModule: DI.Module
    get() = DI.Module("stores.module") {
        bind<StoresViewModel>() with factory {
            StoresViewModel(instance())
        }

        bind<StoresUseCase>() with factory {
            // PRODUCTION gets its own implementation once the stores move to Firebase.
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockStoresUseCase()
                AppEnvironment.MOCK -> MockStoresUseCase()
            }
        }
    }
