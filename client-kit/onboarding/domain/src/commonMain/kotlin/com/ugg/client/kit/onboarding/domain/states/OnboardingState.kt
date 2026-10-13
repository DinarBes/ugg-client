package com.ugg.client.kit.onboarding.domain.states

import com.ugg.client.kit.onboarding.domain.entities.OnboardingSlide

data class OnboardingState(
    val slides: List<OnboardingSlide> = emptyList(),
    val page: Int = 0,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
