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
        // A store without a phone in the database has an empty link; nothing to open then.
        // runCatching: openUri throws when no app on the device can handle the link.
        onRoute = { store -> store.routeUri.takeIf { it.isNotEmpty() }?.let { runCatching { uriHandler.openUri(it) } } },
        onCall = { store -> store.phoneUri.takeIf { it.isNotEmpty() }?.let { runCatching { uriHandler.openUri(it) } } },
    )
}
