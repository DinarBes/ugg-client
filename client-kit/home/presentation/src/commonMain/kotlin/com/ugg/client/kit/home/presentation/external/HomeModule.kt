package com.ugg.client.kit.home.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.home.data.useCases.MockHomeUseCase
import com.ugg.client.kit.home.data.useCases.SharedHomeUseCase
import com.ugg.client.kit.home.domain.useCases.HomeUseCase
import com.ugg.client.kit.home.presentation.external.view.models.HomeViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

val homeModule: DI.Module
    get() = DI.Module("home.module") {
        bind<HomeViewModel>() with factory {
            HomeViewModel(instance(), instance())
        }

        bind<HomeUseCase>() with factory {
            // PRODUCTION reads Firestore (CatalogService, scoped in core/data); MOCK keeps the bundled content.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedHomeUseCase(on(core).instance())
                AppEnvironment.MOCK -> MockHomeUseCase()
            }
        }
    }
