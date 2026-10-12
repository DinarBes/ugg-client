package com.ugg.client.kit.splash.presentation.external

import com.ugg.client.kit.splash.data.splashDataModule
import com.ugg.client.kit.splash.presentation.external.view.models.SplashViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val splashModule: DI.Module
    get() = DI.Module("splash.module") {
        import(splashDataModule)

        bind<SplashViewModel>() with factory {
            SplashViewModel(instance(), instance())
        }
    }
