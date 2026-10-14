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
 * Profile tab. A guest sees an offer to sign in; a signed-in user sees the profile and the order
 * in progress. «Выйти» is handled here; the other rows and cards are the host's decision.
 */
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    onSignIn: () -> Unit = {},
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
        isGuest = state.isGuest,
        isLoading = state.isLoading,
        error = state.error,
        contentPadding = contentPadding,
        onRetry = { viewModel.observe() },
        onSignIn = onSignIn,
        onOpenSettings = onOpenSettings,
        onOpenOrder = onOpenOrder,
        onOpenSize = onOpenSize,
        onOpenOrders = onOpenOrders,
        onMenuItem = { kind ->
            if (kind == ProfileMenuItem.Kind.Logout) viewModel.signOut() else onMenuItem(kind)
        },
    )
}
