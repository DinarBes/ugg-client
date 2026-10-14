package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.profile.presentation.external.view.models.NameViewModel
import com.ugg.client.kit.profile.presentation.internal.view.NameView
import org.kodein.di.compose.viewmodel.rememberViewModel

/** Changing the name. Saving closes the screen. */
@Composable
fun NameScreen(onBack: () -> Unit) {
    val viewModel: NameViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    // The saved name on every visit, but not on rotation: the flag survives it, a new visit starts without it.
    var isStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isStarted) {
            isStarted = true
            viewModel.load()
        }
    }

    NameView(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onSave = viewModel::onSave,
    )
}
