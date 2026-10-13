package com.ugg.client.kit.splash.data.repositories

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.auth.context.AuthEvent
import com.ugg.client.infrastructure.keystore.session.AccessService
import com.ugg.client.infrastructure.keystore.session.AuthorizationStatus
import com.ugg.client.kit.onboarding.domain.useCases.OnboardingUseCase
import com.ugg.client.kit.splash.domain.entities.SplashFlow
import com.ugg.client.kit.splash.domain.exceptions.KeystoreUnavailableException
import com.ugg.client.kit.splash.domain.useCases.SplashUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

class SharedSplashRepository(
    private val auth: TemplateAuth,
    private val access: AccessService,
    private val onboarding: OnboardingUseCase,
): SplashUseCase {
    private val context by auth.context

    override suspend fun getInitialFlow(): SplashFlow {
        delay(2.seconds)

        if (!onboarding.isCompleted()) return SplashFlow.Onboarding

        return when (access.authorizationStatus()) {
            AuthorizationStatus.Authorized -> SplashFlow.Launch
            AuthorizationStatus.NotAuthorized -> SplashFlow.Auth
            // Credentials unreadable — most likely a background launch on a locked device. Throw
            // rather than guess: the splash screen stays put and no one gets signed out.
            AuthorizationStatus.Undetermined -> throw KeystoreUnavailableException()
        }
    }

    override fun flows(): Flow<SplashFlow> = context.event.map {
        when (it) {
            AuthEvent.Login -> SplashFlow.Launch
            AuthEvent.Logout -> SplashFlow.Auth
        }
    }
}
