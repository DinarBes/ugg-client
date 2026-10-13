package com.ugg.client.kit.product.presentation.external.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.product.presentation.external.view.models.ProductViewModel
import com.ugg.client.kit.product.presentation.internal.view.ProductView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * The single model with its options.
 *
 * @param contentPadding insets from the host; the gallery ignores the top one and goes under the
 *   status bar, the bottom one keeps the «В корзину» bar above the tab bar.
 * @param onBack `null` hides the back button — e.g. when the screen is a tab.
 * @param onAddToCart called once the selection is in the cart.
 */
@Composable
fun ProductScreen(
    contentPadding: PaddingValues,
    onAddToCart: () -> Unit,
    onBack: (() -> Unit)? = null,
    onShare: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onOpenSizeChart: () -> Unit = {},
) {
    val viewModel: ProductViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    ProductView(
        product = state.product,
        selection = state.selection,
        expandedDetail = state.expandedDetail,
        isLoading = state.isLoading,
        error = state.error,
        contentPadding = contentPadding,
        onRetry = { viewModel.fetch() },
        onBack = onBack,
        onShare = onShare,
        onToggleFavorite = onToggleFavorite,
        onSelectShade = { viewModel.selectShade(it) },
        onSelectHeight = { viewModel.selectHeight(it) },
        onSelectSize = { viewModel.selectSize(it) },
        onOpenSizeChart = onOpenSizeChart,
        onToggleDetail = { viewModel.toggleDetail(it) },
        onAddToCart = { viewModel.addToCart(onAddToCart) },
    )
}
