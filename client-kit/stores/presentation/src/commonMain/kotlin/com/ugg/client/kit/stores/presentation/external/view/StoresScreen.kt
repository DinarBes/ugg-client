package com.ugg.client.kit.stores.presentation.external.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import com.ugg.client.kit.stores.presentation.external.view.models.StoresViewModel
import com.ugg.client.kit.stores.presentation.internal.view.StoresView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * «Наши точки»: map plus a card per store. «Маршрут» and «Позвонить» hand off to the system
 * (Yandex Maps or the browser, the dialer) — the app has no screens for them.
 */
@Composable
fun StoresScreen(contentPadding: PaddingValues) {
    val viewModel: StoresViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    StoresView(
        stores = state.visibleStores,
        cities = state.cities,
        totalCount = state.stores.size,
        city = state.city,
        selectedStoreId = state.selectedStoreId,
        isLoading = state.isLoading,
        error = state.error,
        contentPadding = contentPadding,
        onRetry = { viewModel.fetch() },
        onSelectCity = { viewModel.selectCity(it) },
        onSelectStore = { viewModel.selectStore(it) },
        onRoute = { uriHandler.openUri(it.routeUri) },
        onCall = { uriHandler.openUri(it.phoneUri) },
    )
}
