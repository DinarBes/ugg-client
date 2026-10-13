package com.ugg.client.kit.onboarding.data.useCases

import com.ugg.client.infrastructure.keystore.foundation.external.InMemoryKeystore
import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreAccessibility
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreConfiguration
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreWriteResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockOnboardingUseCaseTest {

    private class FixedKeystore(private val result: KeystoreReadResult) : Keystore {
        override val configuration = KeystoreConfiguration()

        override fun get(key: String): KeystoreReadResult = result

        override fun set(
            key: String,
            value: String?,
            accessibility: KeystoreAccessibility,
        ): KeystoreWriteResult = KeystoreWriteResult.Stored
    }

    @Test
    fun freshInstallIsNotCompleted() = runTest {
        assertFalse(MockOnboardingUseCase(InMemoryKeystore()).isCompleted())
    }

    @Test
    fun completeIsRemembered() = runTest {
        val keystore = InMemoryKeystore()

        MockOnboardingUseCase(keystore).complete()

        assertTrue(MockOnboardingUseCase(keystore).isCompleted())
    }

    @Test
    fun unreadableKeystoreDoesNotShowTheOnboardingAgain() = runTest {
        val repository = MockOnboardingUseCase(FixedKeystore(KeystoreReadResult.TemporarilyUnavailable))

        assertTrue(repository.isCompleted())
    }

    @Test
    fun everySlideHasCopy() = runTest {
        val slides = MockOnboardingUseCase(InMemoryKeystore()).slides()

        assertTrue(slides.isNotEmpty())
        assertTrue(slides.all { it.id.isNotBlank() && it.title.isNotBlank() && it.description.isNotBlank() })
        assertTrue(slides.map { it.id }.toSet().size == slides.size)
    }
}
