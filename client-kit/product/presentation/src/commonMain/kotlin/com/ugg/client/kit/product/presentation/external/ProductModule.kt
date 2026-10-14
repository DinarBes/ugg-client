package com.ugg.client.kit.product.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.product.data.useCases.MockProductUseCase
import com.ugg.client.kit.product.data.useCases.SharedProductUseCase
import com.ugg.client.kit.product.domain.useCases.ProductUseCase
import com.ugg.client.kit.product.presentation.external.view.models.ProductViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

val productModule: DI.Module
    get() = DI.Module("product.module") {
        bind<ProductViewModel>() with factory {
            ProductViewModel(instance(), instance())
        }

        bind<ProductUseCase>() with factory {
            // PRODUCTION reads Firestore (CatalogService, scoped in core/data); MOCK keeps the bundled product.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedProductUseCase(on(core).instance())
                AppEnvironment.MOCK -> MockProductUseCase()
            }
        }
    }
