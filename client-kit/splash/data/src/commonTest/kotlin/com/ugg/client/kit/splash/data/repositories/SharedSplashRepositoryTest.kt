package com.ugg.client.kit.splash.data.repositories

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.infrastructure.firebase.auth.AuthUser
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneVerification
import com.ugg.client.infrastructure.keystore.foundation.external.InMemoryKeystore
import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreConfiguration
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreWriteResult
import com.ugg.client.infrastructure.keystore.session.AccessService
import com.ugg.client.kit.onboarding.data.useCases.MockOnboardingUseCase
import com.ugg.client.kit.splash.domain.entities.SplashFlow
import com.ugg.client.kit.splash.domain.exceptions.KeystoreUnavailableException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class SharedSplashRepositoryTest {

    private class FixedKeystore(private val result: KeystoreReadResult) : Keystore {
        override val configuration = KeystoreConfiguration()

        override fun get(key: String): KeystoreReadResult = result

        override fun set(
            key: String,
            value: String?,
            accessibility: KeystoreAccessibility,
        ): KeystoreWriteResult = KeystoreWriteResult.Stored
    }

    private class GuestPhoneAuth : PhoneAuthService {
        override fun observeUser(): Flow<AuthUser?> = flowOf(null)
        override val currentUser: AuthUser? = null
        override suspend fun sendCode(phone: String): PhoneVerification = error("not used")
        override suspend fun verify(verificationId: String, code: String): AuthUser = error("not used")
        override suspend fun signOut() = Unit
    }

    private suspend fun repositoryFor(
        result: KeystoreReadResult,
        onboardingCompleted: Boolean = true,
    ): SharedSplashRepository {
        val auth = TemplateAuth(TemplateCore { })
        val access = AccessService(FixedKeystore(result))
        val onboarding = MockOnboardingUseCase(InMemoryKeystore())
        if (onboardingCompleted) onboarding.complete()

        return SharedSplashRepository(auth, access, onboarding, GuestPhoneAuth())
    }

    @Test
    fun firstLaunchShowsTheOnboarding() = runTest {
        val repository = repositoryFor(KeystoreReadResult.NotFound, onboardingCompleted = false)

        assertEquals(SplashFlow.Onboarding, repository.getInitialFlow())
    }

    @Test
    fun unreadableKeystoreDoesNotSendTheUserToAuth() = runTest {
        assertFailsWith<KeystoreUnavailableException> {
            repositoryFor(KeystoreReadResult.TemporarilyUnavailable).getInitialFlow()
        }
    }

    @Test
    fun missingTokenLetsTheGuestIn() = runTest {
        assertEquals(SplashFlow.Launch, repositoryFor(KeystoreReadResult.NotFound).getInitialFlow())
    }

    @Test
    fun storedTokenLaunchesTheApp() = runTest {
        assertEquals(SplashFlow.Launch, repositoryFor(KeystoreReadResult.Found("refresh")).getInitialFlow())
    }
}
