package com.ugg.client.kit.onboarding.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.onboarding.domain.entities.OnboardingSlide
import com.ugg.client.kit.onboarding.domain.states.OnboardingState
import com.ugg.client.kit.onboarding.domain.useCases.OnboardingUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class OnboardingViewModel(
    private val useCase: OnboardingUseCase,
) : ViewModel() {
    val state: StateFlow<OnboardingState>
        field = MutableStateFlow(OnboardingState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@OnboardingViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    init {
        fetch()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            show(useCase.slides())
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }

    fun setPage(page: Int) = state.update { it.copy(page = page) }

    /**
     * Remembers that the onboarding is done and only then leaves it: navigating first would show
     * it again on the next launch if the app were killed in between.
     */
    fun finish(next: () -> Unit) = viewModelScope.launch(unexpectedExceptionHandler) {
        useCase.complete()

        next()
    }

    private fun show(slides: List<OnboardingSlide>) = state.update { it.copy(slides = slides) }
}

@Suppress("unused")
fun DI.onboardingViewModel(): OnboardingViewModel = this.direct.instance()
