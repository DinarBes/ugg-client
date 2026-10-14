package com.ugg.client.kit.auth.presentation.external

import com.ugg.client.kit.auth.data.authDataModule
import com.ugg.client.kit.auth.domain.useCases.AuthUseCase
import com.ugg.client.kit.auth.presentation.external.view.models.InitiateViewModel
import com.ugg.client.kit.auth.presentation.external.view.models.OtpViewModel
import com.ugg.client.kit.auth.presentation.external.view.models.RegistrationViewModel
import com.ugg.client.kit.core.domain.AppEnvironment
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.singleton

val authModule: DI.Module
    get() = DI.Module("auth.module") {
        import(authDataModule)

        bind<InitiateViewModel>() with factory {
            InitiateViewModel(instance(), instance())
        }

        bind<OtpViewModel>() with factory {
            OtpViewModel(instance())
        }

        bind<RegistrationViewModel>() with factory {
            RegistrationViewModel(instance(), instance())
        }
    }
