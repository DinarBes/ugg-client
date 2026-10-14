package com.ugg.client.kit.story.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.story.presentation.external.view.models.StoryViewModel
import com.ugg.client.kit.story.presentation.internal.view.StoryView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * «История мастерской». A full-screen page over the tabs: it handles the system bar insets itself.
 *
 * @param onChooseModel the «Выбрать свою пару» button at the bottom.
 */
@Composable
fun StoryScreen(
    onBack: () -> Unit,
    onChooseModel: () -> Unit,
) {
    val viewModel: StoryViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    StoryView(
        story = state.story,
        isLoading = state.isLoading,
        error = state.error,
        onRetry = { viewModel.fetch() },
        onBack = onBack,
        onChooseModel = onChooseModel,
    )
}
