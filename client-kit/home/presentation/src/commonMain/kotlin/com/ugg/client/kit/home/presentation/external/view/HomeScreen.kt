package com.ugg.client.kit.home.presentation.external.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.home.presentation.external.view.models.HomeViewModel
import com.ugg.client.kit.home.presentation.internal.view.HomeView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Home tab. Where each block leads is the host's decision — the home screen knows nothing about
 * the other tabs.
 *
 * @param contentPadding insets from the host (status bar, tab bar).
 */
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onChooseModel: () -> Unit,
    onOpenStores: () -> Unit,
    onOpenStory: () -> Unit = {},
    onOpenCare: () -> Unit = {},
    onOpenCart: () -> Unit = {},
) {
    val viewModel: HomeViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    HomeView(
        home = state.home,
        cartCount = state.cartCount,
        isLoading = state.isLoading,
        isRefreshing = state.isRefreshing,
        error = state.error,
        contentPadding = contentPadding,
        onRetry = { viewModel.fetch() },
        onRefresh = { viewModel.refresh() },
        onChooseModel = onChooseModel,
        onOpenStores = onOpenStores,
        onOpenStory = onOpenStory,
        onOpenCare = onOpenCare,
        onOpenCart = onOpenCart,
    )
}
