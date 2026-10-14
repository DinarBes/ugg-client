package com.ugg.client.kit.stores.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.stores.data.useCases.MockStoresUseCase
import com.ugg.client.kit.stores.data.useCases.SharedStoresUseCase
import com.ugg.client.kit.stores.domain.useCases.StoresUseCase
import com.ugg.client.kit.stores.presentation.external.view.models.StoresViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

val storesModule: DI.Module
    get() = DI.Module("stores.module") {
        bind<StoresViewModel>() with factory {
            StoresViewModel(instance())
        }

        bind<StoresUseCase>() with factory {
            // PRODUCTION reads Firestore (CatalogService, scoped in core/data); MOCK keeps the bundled stores.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedStoresUseCase(on(core).instance())
                AppEnvironment.MOCK -> MockStoresUseCase()
            }
        }
    }
