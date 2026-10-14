package com.ugg.client.kit.care.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.care.data.useCases.MockCareUseCase
import com.ugg.client.kit.care.data.useCases.SharedCareUseCase
import com.ugg.client.kit.care.domain.useCases.CareUseCase
import com.ugg.client.kit.care.presentation.external.view.models.CareViewModel
import com.ugg.client.kit.core.domain.AppEnvironment
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

val careModule: DI.Module
    get() = DI.Module("care.module") {
        bind<CareViewModel>() with factory {
            CareViewModel(instance())
        }

        bind<CareUseCase>() with factory {
            // PRODUCTION reads Firestore (CatalogService, scoped in core/data); MOCK keeps the bundled content.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedCareUseCase(on(core).instance())
                AppEnvironment.MOCK -> MockCareUseCase()
            }
        }
    }
