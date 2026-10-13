package com.ugg.client.kit.onboarding.domain.useCases

import com.ugg.client.kit.onboarding.domain.entities.OnboardingSlide

interface OnboardingUseCase {
    suspend fun slides(): List<OnboardingSlide>

    /** Whether the user has already left the onboarding once; the splash skips it if so. */
    suspend fun isCompleted(): Boolean

    suspend fun complete()
}
