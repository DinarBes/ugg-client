package com.ugg.client.kit.cart.presentation.external

import com.ugg.client.kit.cart.data.storage.CartStorage
import com.ugg.client.kit.cart.data.useCases.MockCartUseCase
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import com.ugg.client.kit.cart.presentation.external.view.models.CartViewModel
import com.ugg.client.kit.core.domain.AppEnvironment
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.singleton

val cartModule: DI.Module
    get() = DI.Module("cart.module") {
        // Singleton on purpose: the product page, the home badge and the cart screen must all
        // read and write the same cart.
        bind<CartStorage>() with singleton { CartStorage() }

        bind<CartUseCase>() with factory {
            // PRODUCTION gets its own implementation once orders go to the backend.
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockCartUseCase(instance())
                AppEnvironment.MOCK -> MockCartUseCase(instance())
            }
        }

        bind<CartViewModel>() with factory {
            CartViewModel(instance())
        }
    }
