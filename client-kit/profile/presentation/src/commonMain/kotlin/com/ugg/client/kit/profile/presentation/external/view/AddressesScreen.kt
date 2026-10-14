package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.profile.presentation.external.view.models.AddressesViewModel
import com.ugg.client.kit.profile.presentation.internal.view.AddressesView
import org.kodein.di.compose.viewmodel.rememberViewModel

/** «Адреса доставки»: the list; adding and editing open the address form. */
@Composable
fun AddressesScreen(onBack: () -> Unit) {
    val viewModel: AddressesViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    AddressesView(
        state = state,
        onBack = onBack,
        onRetry = { viewModel.observe() },
        onRefresh = { viewModel.refresh() },
        onOpen = viewModel::onOpen,
        onAdd = { viewModel.onAdd() },
    )
}
