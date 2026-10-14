package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.profile.presentation.external.view.models.SizeViewModel
import com.ugg.client.kit.profile.presentation.internal.view.SizeView
import org.kodein.di.compose.viewmodel.rememberViewModel

/** «Мой размер». Saving closes the screen. */
@Composable
fun SizeScreen(onBack: () -> Unit) {
    val viewModel: SizeViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    // Fresh data on every visit, but not on rotation: the flag survives it, a new visit starts without it.
    var isStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isStarted) {
            isStarted = true
            viewModel.load()
        }
    }

    SizeView(
        state = state,
        onBack = onBack,
        onRetry = { viewModel.load() },
        onSelectSize = viewModel::onSelectSize,
        onSelectHeight = viewModel::onSelectHeight,
        onFootLengthChange = viewModel::onFootLengthChange,
        onSave = viewModel::onSave,
    )
}
