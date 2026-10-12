package com.ugg.client.kit.splash.data.repositories

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.core.TemplateCore
import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreConfiguration
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreWriteResult
import com.ugg.client.infrastructure.keystore.session.AccessService
import com.ugg.client.kit.splash.domain.entities.SplashFlow
import com.ugg.client.kit.splash.domain.exceptions.KeystoreUnavailableException
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private fun repositoryFor(result: KeystoreReadResult): SharedSplashRepository {
        val auth = TemplateAuth(TemplateCore { })
        val access = AccessService(FixedKeystore(result))
        return SharedSplashRepository(auth, access)
    }

    @Test
    fun unreadableKeystoreDoesNotSendTheUserToAuth() = runTest {
        assertFailsWith<KeystoreUnavailableException> {
            repositoryFor(KeystoreReadResult.TemporarilyUnavailable).getInitialFlow()
        }
    }

    @Test
    fun missingTokenSendsTheUserToAuth() = runTest {
        assertEquals(SplashFlow.Auth, repositoryFor(KeystoreReadResult.NotFound).getInitialFlow())
    }

    @Test
    fun storedTokenLaunchesTheApp() = runTest {
        assertEquals(SplashFlow.Launch, repositoryFor(KeystoreReadResult.Found("refresh")).getInitialFlow())
    }
}
