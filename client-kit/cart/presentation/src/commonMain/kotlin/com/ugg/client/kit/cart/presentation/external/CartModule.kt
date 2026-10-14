package com.ugg.client.kit.cart.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.cart.data.storage.CartStorage
import com.ugg.client.kit.cart.data.useCases.MockCartUseCase
import com.ugg.client.kit.cart.data.useCases.SharedCartUseCase
import com.ugg.client.kit.cart.domain.useCases.CartUseCase
import com.ugg.client.kit.cart.presentation.external.view.models.CartViewModel
import com.ugg.client.kit.core.domain.AppEnvironment
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.singleton

val cartModule: DI.Module
    get() = DI.Module("cart.module") {
        // Singleton on purpose: the product page, the home badge and the cart screen must all
        // read and write the same cart.
        bind<CartStorage>() with singleton { CartStorage() }

        bind<CartUseCase>() with factory {
            // PRODUCTION places real orders in Firestore (services scoped in core/data); the cart
            // itself stays on the device in both.
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedCartUseCase(
                    instance(),
                    on(core).instance(),
                    on(core).instance(),
                    on(core).instance()
                )
                AppEnvironment.MOCK -> MockCartUseCase(instance())
            }
        }

        bind<CartViewModel>() with factory {
            CartViewModel(instance())
        }
    }
