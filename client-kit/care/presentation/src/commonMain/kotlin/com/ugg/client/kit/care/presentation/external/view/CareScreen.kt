package com.ugg.client.kit.care.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.care.presentation.external.view.models.CareViewModel
import com.ugg.client.kit.care.presentation.internal.view.CareView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * «Уход за уггами».
 *
 * @param onContact «Написать в мастерскую» on the bottom card; where it leads is the host's decision.
 */
@Composable
fun CareScreen(
    onBack: () -> Unit,
    onContact: () -> Unit = {},
) {
    val viewModel: CareViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    CareView(
        care = state.care,
        isLoading = state.isLoading,
        error = state.error,
        onRetry = { viewModel.fetch() },
        onBack = onBack,
        onContact = onContact,
    )
}
