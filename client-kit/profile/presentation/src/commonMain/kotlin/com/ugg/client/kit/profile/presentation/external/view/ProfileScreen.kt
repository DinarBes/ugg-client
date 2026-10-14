package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.core.presentation.external.components.UggAlertDialog
import com.ugg.client.kit.profile.domain.entities.ProfileMenuItem
import com.ugg.client.kit.profile.presentation.external.view.models.ProfileViewModel
import com.ugg.client.kit.profile.presentation.internal.view.ProfileView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Profile tab. A guest sees an offer to sign in; a signed-in user sees the profile and the order
 * in progress. «Выйти» is handled here, after a confirmation; the other rows and cards are the host's decision.
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
    var isConfirmingSignOut by rememberSaveable { mutableStateOf(false) }

    ProfileView(
        profile = state.profile,
        isGuest = state.isGuest,
        isLoading = state.isLoading,
        isRefreshing = state.isRefreshing,
        error = state.error,
        contentPadding = contentPadding,
        onRetry = { viewModel.observe() },
        onRefresh = { viewModel.refresh() },
        onSignIn = onSignIn,
        onOpenSettings = onOpenSettings,
        onOpenOrder = onOpenOrder,
        onOpenSize = onOpenSize,
        onOpenOrders = onOpenOrders,
        onMenuItem = { kind ->
            if (kind == ProfileMenuItem.Kind.Logout) isConfirmingSignOut = true else onMenuItem(kind)
        },
    )

    if (isConfirmingSignOut) {
        UggAlertDialog(
            title = "Выйти из аккаунта?",
            text = "Заказы и размер сохранятся — увидите их, когда войдёте по тому же номеру.",
            confirmText = "Выйти",
            dismissText = "Остаться",
            onConfirm = {
                isConfirmingSignOut = false
                viewModel.signOut()
            },
            onDismiss = { isConfirmingSignOut = false },
        )
    }
}
