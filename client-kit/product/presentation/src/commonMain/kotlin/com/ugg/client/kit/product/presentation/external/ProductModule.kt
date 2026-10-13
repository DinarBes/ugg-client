package com.ugg.client.kit.product.presentation.external

import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.product.data.useCases.MockProductUseCase
import com.ugg.client.kit.product.domain.useCases.ProductUseCase
import com.ugg.client.kit.product.presentation.external.view.models.ProductViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val productModule: DI.Module
    get() = DI.Module("product.module") {
        bind<ProductViewModel>() with factory {
            ProductViewModel(instance(), instance())
        }

        bind<ProductUseCase>() with factory {
            // PRODUCTION gets its own implementation once the product moves to Firebase.
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockProductUseCase()
                AppEnvironment.MOCK -> MockProductUseCase()
            }
        }
    }
