package com.ugg.client.kit.onboarding.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.onboarding.presentation.external.view.models.OnboardingViewModel
import com.ugg.client.kit.onboarding.presentation.internal.view.OnboardingView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Where the buttons lead is the host's decision: the onboarding knows nothing about the screens
 * after it.
 *
 * @param onStart «Начать» — continue into the app.
 * @param onLogin «Войти по номеру телефона».
 */
@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onLogin: () -> Unit,
) {
    val viewModel: OnboardingViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    OnboardingView(
        slides = state.slides,
        page = state.page,
        error = state.error,
        onPageChanged = { viewModel.setPage(it) },
        onStart = { viewModel.finish(onStart) },
        onLogin = { viewModel.finish(onLogin) },
        onRetry = { viewModel.fetch() },
    )
}
