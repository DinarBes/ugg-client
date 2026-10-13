package com.ugg.client.kit.onboarding.presentation.external

import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.onboarding.data.useCases.MockOnboardingUseCase
import com.ugg.client.kit.onboarding.domain.useCases.OnboardingUseCase
import com.ugg.client.kit.onboarding.presentation.external.view.models.OnboardingViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val onboardingModule: DI.Module
    get() = DI.Module("onboarding.module") {
        bind<OnboardingViewModel>() with factory {
            OnboardingViewModel(instance())
        }

        bind<OnboardingUseCase>() with factory {
            when(instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> MockOnboardingUseCase(instance())
                AppEnvironment.MOCK -> MockOnboardingUseCase(instance())
            }
        }
    }
