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
import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
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

    /** Starts as a guest; [signIn] plays a successful code confirmation. */
    private class FakePhoneAuth : PhoneAuthService {
        private val user = MutableStateFlow<AuthUser?>(null)
        fun signIn(uid: String) = user.update { AuthUser(uid = uid, phone = "+79001234567") }
        override fun observeUser(): Flow<AuthUser?> = user
        override val currentUser: AuthUser? get() = user.value
        override suspend fun sendCode(phone: String): PhoneVerification = error("not used")
        override suspend fun verify(verificationId: String, code: String): AuthUser = error("not used")
        override suspend fun signOut() = user.update { null }
        override suspend fun deleteAccount() = Unit
    }

    private class FakeUsers(
        existing: Map<String, UserDto> = emptyMap(),
        private val failure: FirestoreException? = null,
    ) : UserService {
        val saved = existing.toMutableMap()
        override fun observeUser(uid: String): Flow<UserDto?> = flowOf(saved[uid])
        override suspend fun getUser(uid: String): UserDto? = failure?.let { throw it } ?: saved[uid]
        override suspend fun saveUser(uid: String, user: UserDto) { saved[uid] = user }
        override fun observeAddresses(uid: String): Flow<List<AddressDto>> = flowOf(emptyList())
        override suspend fun saveAddress(uid: String, address: AddressDto) = ""
        override suspend fun deleteAddress(uid: String, addressId: String) = Unit
        override suspend fun deleteUser(uid: String) = Unit
    }

    private suspend fun repositoryFor(
        result: KeystoreReadResult,
        onboardingCompleted: Boolean = true,
        phoneAuth: PhoneAuthService = FakePhoneAuth(),
        users: UserService = FakeUsers(),
    ): SharedSplashRepository {
        val auth = TemplateAuth(TemplateCore { })
        val access = AccessService(FixedKeystore(result))
        val onboarding = MockOnboardingUseCase(InMemoryKeystore())
        if (onboardingCompleted) onboarding.complete()

        return SharedSplashRepository(auth, access, onboarding, phoneAuth, users)
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

    @Test
    fun firstSignInWithANewNumberAsksForTheName() = runTest {
        val phoneAuth = FakePhoneAuth()
        val users = FakeUsers()
        val repository = repositoryFor(KeystoreReadResult.NotFound, phoneAuth = phoneAuth, users = users)

        val next = backgroundScope.async { repository.flows().first() }
        testScheduler.runCurrent()
        phoneAuth.signIn("new-user")

        assertEquals(SplashFlow.Registration, next.await())
        assertEquals("+79001234567", users.saved.getValue("new-user").phone)
    }

    @Test
    fun knownNumberGoesStraightToTheApp() = runTest {
        val phoneAuth = FakePhoneAuth()
        val users = FakeUsers(existing = mapOf("old-user" to UserDto(name = "Анна")))
        val repository = repositoryFor(KeystoreReadResult.NotFound, phoneAuth = phoneAuth, users = users)

        val next = backgroundScope.async { repository.flows().first() }
        testScheduler.runCurrent()
        phoneAuth.signIn("old-user")

        assertEquals(SplashFlow.Launch, next.await())
        assertEquals("Анна", users.saved.getValue("old-user").name)
    }

    @Test
    fun offlineSignInSkipsTheNameStep() = runTest {
        val phoneAuth = FakePhoneAuth()
        val users = FakeUsers(failure = FirestoreException(FirestoreException.Reason.Unavailable))
        val repository = repositoryFor(KeystoreReadResult.NotFound, phoneAuth = phoneAuth, users = users)

        val next = backgroundScope.async { repository.flows().first() }
        testScheduler.runCurrent()
        phoneAuth.signIn("someone")

        assertEquals(SplashFlow.Launch, next.await())
    }
}
