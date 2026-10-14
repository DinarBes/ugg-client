package com.ugg.client.kit.profile.presentation.external

import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.profile.data.useCases.MockAddressesUseCase
import com.ugg.client.kit.profile.data.useCases.MockProfileUseCase
import com.ugg.client.kit.profile.data.useCases.MockSettingsUseCase
import com.ugg.client.kit.profile.data.useCases.MockSizeUseCase
import com.ugg.client.kit.profile.data.useCases.SharedAddressesUseCase
import com.ugg.client.kit.profile.data.useCases.SharedProfileUseCase
import com.ugg.client.kit.profile.data.useCases.SharedSettingsUseCase
import com.ugg.client.kit.profile.data.useCases.SharedSizeUseCase
import com.ugg.client.kit.profile.domain.useCases.AddressesUseCase
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import com.ugg.client.kit.profile.domain.useCases.SettingsUseCase
import com.ugg.client.kit.profile.domain.useCases.SizeUseCase
import com.ugg.client.kit.profile.presentation.external.view.models.AddressFormViewModel
import com.ugg.client.kit.profile.presentation.external.view.models.AddressesViewModel
import com.ugg.client.kit.profile.presentation.external.view.models.NameViewModel
import com.ugg.client.kit.profile.presentation.external.view.models.ProfileViewModel
import com.ugg.client.kit.profile.presentation.external.view.models.SettingsViewModel
import com.ugg.client.kit.profile.presentation.external.view.models.SizeViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on

// MOCK keeps one in-memory store per app run, shared by the list, the form and the profile.
// Not DI bindings: Kodein matches subtypes, so `bind<MockSizeUseCase>` would clash with `bind<SizeUseCase>`.
private val mockSize by lazy { MockSizeUseCase() }
private val mockAddresses by lazy { MockAddressesUseCase() }
private val mockSettings by lazy { MockSettingsUseCase() }

val profileModule: DI.Module
    get() = DI.Module("profile.module") {

        bind<ProfileViewModel>() with factory {
            ProfileViewModel(instance())
        }

        bind<SizeViewModel>() with factory {
            SizeViewModel(instance(), instance())
        }

        bind<AddressesViewModel>() with factory {
            AddressesViewModel(instance(), instance())
        }

        bind<AddressFormViewModel>() with factory {
            AddressFormViewModel(instance(), instance())
        }

        bind<SettingsViewModel>() with factory {
            SettingsViewModel(instance(), instance(), instance())
        }

        bind<NameViewModel>() with factory {
            NameViewModel(instance(), instance())
        }

        bind<ProfileUseCase>() with factory {
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedProfileUseCase(
                    auth = on(core).instance(),
                    users = on(core).instance(),
                    orders = on(core).instance(),
                )
                AppEnvironment.MOCK -> MockProfileUseCase()
            }
        }

        bind<SizeUseCase>() with factory {
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedSizeUseCase(
                    auth = on(core).instance(),
                    users = on(core).instance(),
                    catalog = on(core).instance(),
                )
                AppEnvironment.MOCK -> mockSize
            }
        }

        bind<AddressesUseCase>() with factory {
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedAddressesUseCase(auth = on(core).instance(), users = on(core).instance())
                AppEnvironment.MOCK -> mockAddresses
            }
        }

        bind<SettingsUseCase>() with factory {
            val core: TemplateCore = instance()

            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedSettingsUseCase(auth = on(core).instance(), users = on(core).instance())
                AppEnvironment.MOCK -> mockSettings
            }
        }
    }
