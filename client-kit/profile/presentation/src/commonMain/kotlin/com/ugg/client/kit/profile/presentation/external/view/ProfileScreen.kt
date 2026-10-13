package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ugg.client.kit.profile.domain.entities.ProfileMenuItem
import com.ugg.client.kit.profile.presentation.external.view.models.ProfileViewModel
import com.ugg.client.kit.profile.presentation.internal.view.ProfileView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Profile tab. A static frame for now: the data is a stub and the actions lead nowhere until
 * accounts and orders come from the backend.
 */
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    onOpenSettings: () -> Unit = {},
    onOpenOrder: () -> Unit = {},
    onOpenSize: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    onMenuItem: (ProfileMenuItem.Kind) -> Unit = {},
) {
    val viewModel: ProfileViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    ProfileView(
        profile = state.profile,
        isLoading = state.isLoading,
        error = state.error,
        contentPadding = contentPadding,
        onRetry = { viewModel.fetch() },
        onOpenSettings = onOpenSettings,
        onOpenOrder = onOpenOrder,
        onOpenSize = onOpenSize,
        onOpenOrders = onOpenOrders,
        onMenuItem = onMenuItem,
    )
}
