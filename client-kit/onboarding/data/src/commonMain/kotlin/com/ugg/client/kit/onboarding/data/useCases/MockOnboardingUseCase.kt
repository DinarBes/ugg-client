package com.ugg.client.kit.onboarding.data.useCases

import com.ugg.client.infrastructure.keystore.foundation.external.Keystore
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.kit.onboarding.domain.entities.OnboardingSlide
import com.ugg.client.kit.onboarding.domain.useCases.OnboardingUseCase

const val COMPLETED_KEY = "onboarding-completed"

/**
 * The slides ship with the app; only the «already seen» flag is persisted.
 *
 * The flag lives in the keystore because it is the only persistent storage the app has so far.
 * Move it to plain settings storage once one exists — it is not a secret.
 */
class MockOnboardingUseCase(
    private val keystore: Keystore,
) : OnboardingUseCase {

    override suspend fun slides(): List<OnboardingSlide> = OnboardingSlide.mock

    override suspend fun isCompleted(): Boolean = when (keystore.get(COMPLETED_KEY)) {
        is KeystoreReadResult.Found -> true
        KeystoreReadResult.NotFound -> false
        // Unreadable is not "never seen": showing the onboarding again to someone who has most
        // likely passed it is worse than skipping it once.
        KeystoreReadResult.TemporarilyUnavailable, is KeystoreReadResult.Failure -> true
    }

    override suspend fun complete() {
        // A failed write only means the onboarding shows once more — not worth failing the exit.
        keystore.set(COMPLETED_KEY, "true")
    }
}
