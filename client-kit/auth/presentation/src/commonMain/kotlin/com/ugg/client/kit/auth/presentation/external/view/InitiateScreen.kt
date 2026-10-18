package com.ugg.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.auth.presentation.external.view.models.COUNTRY_CODE
import com.ugg.client.kit.auth.presentation.external.view.models.InitiateViewModel
import com.ugg.client.kit.auth.presentation.internal.view.InitiateView
import org.kodein.di.compose.viewmodel.rememberViewModel


/**
 * Phone number step of the sign-in.
 *
 * @param onBack the back button.
 * @param onSkip «Продолжить без входа» — guests can browse and add to the cart.
 */
@Composable
fun InitiateScreen(
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
) {
    val viewModel: InitiateViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    // A fresh form on every visit to this step, but not on rotation or on «back» from the next
    // step: the flag lives as long as this back stack entry.
    var isStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isStarted) {
            isStarted = true
            viewModel.reset()
        }
    }

    InitiateView(
        // The state keeps the full international number the backend expects; the field edits
        // only the national part.
        nationalNumber = state.phone.removePrefix(COUNTRY_CODE),
        countryCode = COUNTRY_CODE,
        error = state.error,
        isLoading = state.isLoading,
        onNationalNumberChanged = viewModel::onNationalNumberChange,
        onSubmit = { viewModel.onSubmit() },
        onBack = onBack,
        onSkip = onSkip,
        onOpenTerms = onOpenTerms,
        onOpenPrivacy = onOpenPrivacy,
    )
}
