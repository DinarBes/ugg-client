package com.ugg.client.kit.profile.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ugg.client.kit.core.presentation.external.components.UggAlertDialog
import com.ugg.client.kit.profile.presentation.external.view.models.AddressFormViewModel
import com.ugg.client.kit.profile.presentation.internal.view.AddressFormView
import org.kodein.di.compose.viewmodel.rememberViewModel

/**
 * Adds an address ([addressId] `null`) or edits a saved one. Saving and deleting close the
 * screen; deleting asks first.
 */
@Composable
fun AddressFormScreen(addressId: String?, onBack: () -> Unit) {
    val viewModel: AddressFormViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()
    var isConfirmingDelete by rememberSaveable { mutableStateOf(false) }

    // A fresh form on every visit, but not on rotation: the flag survives it, a new visit starts without it.
    var isStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!isStarted) {
            isStarted = true
            viewModel.load(addressId)
        }
    }

    AddressFormView(
        state = state,
        onBack = onBack,
        onChange = viewModel::onChange,
        onSave = viewModel::onSave,
        onDelete = { isConfirmingDelete = true },
    )

    if (isConfirmingDelete) {
        UggAlertDialog(
            title = "Удалить адрес?",
            text = state.address.summary,
            confirmText = "Удалить",
            dismissText = "Оставить",
            onConfirm = {
                isConfirmingDelete = false
                viewModel.onDelete()
            },
            onDismiss = { isConfirmingDelete = false },
        )
    }
}
