package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.core.presentation.external.components.UggAlertDialog
import com.ugg.client.kit.profile.presentation.external.view.models.SettingsViewModel
import com.ugg.client.kit.profile.presentation.internal.view.SettingsView
import org.kodein.di.compose.viewmodel.rememberViewModel

private enum class Confirmation { SignOut, Delete }

/** «Настройки»: the name, signing out, deleting the account, the app version. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()
    var confirmation by rememberSaveable { mutableStateOf<Confirmation?>(null) }

    SettingsView(
        state = state,
        onBack = onBack,
        onRefresh = { viewModel.refresh() },
        onEditName = viewModel::onEditName,
        onSignOut = { confirmation = Confirmation.SignOut },
        onDeleteAccount = { confirmation = Confirmation.Delete },
    )

    when (confirmation) {
        Confirmation.SignOut -> UggAlertDialog(
            title = "Выйти из аккаунта?",
            text = "Заказы и размер сохранятся — увидите их, когда войдёте по тому же номеру.",
            confirmText = "Выйти",
            dismissText = "Остаться",
            onConfirm = {
                confirmation = null
                viewModel.onSignOut()
            },
            onDismiss = { confirmation = null },
        )

        Confirmation.Delete -> UggAlertDialog(
            title = "Удалить аккаунт?",
            text = "Сотрём имя, размер и адреса доставки. Заказы останутся у мастерской. По этому номеру можно будет войти снова — как новый покупатель.",
            confirmText = "Удалить навсегда",
            dismissText = "Отмена",
            onConfirm = {
                confirmation = null
                viewModel.onDeleteAccount()
            },
            onDismiss = { confirmation = null },
        )

        null -> Unit
    }
}
