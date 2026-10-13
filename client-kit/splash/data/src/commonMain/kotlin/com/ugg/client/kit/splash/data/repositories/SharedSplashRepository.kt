package com.ugg.client.kit.splash.data.repositories

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.auth.context.AuthEvent
import com.ugg.client.infrastructure.keystore.session.AccessService
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.keystore.session.AuthorizationStatus
import com.ugg.client.kit.onboarding.domain.useCases.OnboardingUseCase
import com.ugg.client.kit.splash.domain.entities.SplashFlow
import com.ugg.client.kit.splash.domain.exceptions.KeystoreUnavailableException
import com.ugg.client.kit.splash.domain.useCases.SplashUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.time.Duration.Companion.seconds

class SharedSplashRepository(
    private val auth: TemplateAuth,
    private val access: AccessService,
    private val onboarding: OnboardingUseCase,
    private val phoneAuth: PhoneAuthService,
): SplashUseCase {
    private val context by auth.context

    override suspend fun getInitialFlow(): SplashFlow {
        delay(2.seconds)

        if (!onboarding.isCompleted()) return SplashFlow.Onboarding

        return when (access.authorizationStatus()) {
            AuthorizationStatus.Authorized -> SplashFlow.Launch
            // Guests browse freely; the phone number is asked for at checkout, not at launch.
            AuthorizationStatus.NotAuthorized -> SplashFlow.Launch
            // Credentials unreadable — most likely a background launch on a locked device. Throw
            // rather than guess: the splash screen stays put and no one gets signed out.
            AuthorizationStatus.Undetermined -> throw KeystoreUnavailableException()
        }
    }

    override fun flows(): Flow<SplashFlow> = merge(
        // MOCK sign-in announces itself through TemplateAuth's context.
        context.event.map {
            when (it) {
                AuthEvent.Login -> SplashFlow.Launch
                // Signing out returns to the app as a guest rather than locking it behind the login.
                AuthEvent.Logout -> SplashFlow.Launch
            }
        },
        // Firebase sign-in is watched directly: it also covers Android verifying the number
        // without a code, which no screen sees. The first value is the state at launch, not a change.
        phoneAuth.observeUser()
            .map { it != null }
            .distinctUntilChanged()
            .drop(1)
            .map { SplashFlow.Launch },
    )
}
