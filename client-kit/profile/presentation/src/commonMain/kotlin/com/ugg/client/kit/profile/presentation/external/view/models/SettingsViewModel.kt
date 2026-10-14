package com.ugg.client.kit.profile.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.core.domain.AppInfo
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.profile.domain.entities.ProfileFlow
import com.ugg.client.kit.profile.domain.states.SettingsState
import com.ugg.client.kit.profile.domain.useCases.SettingsUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val useCase: SettingsUseCase,
    appInfo: AppInfo,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<SettingsState>
        field = MutableStateFlow(SettingsState(version = appInfo.label))

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@SettingsViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false, isRefreshing = false, isDeleting = false) }
    }

    private var observing: Job? = null

    init {
        observe()
    }

    fun observe() = subscribe(isRefresh = false)

    /**
     * Pull-to-refresh. The data is live already (a Firestore listener); this restarts the
     * subscription, so a broken one (network was lost, rules changed) gets a fresh start.
     */
    fun refresh() {
        if (state.value.isRefreshing) return
        // Set right away, not inside the coroutine: a second pull before it starts is ignored.
        state.update { it.copy(isRefreshing = true) }
        subscribe(isRefresh = true)
    }

    private fun subscribe(isRefresh: Boolean) {
        observing?.cancel()
        observing = viewModelScope.launch(unexpectedExceptionHandler) {
            if (!isRefresh) state.update { it.copy(isLoading = true, error = null) }

            useCase.observeAccount().collect { account ->
                // The error is kept: a failed account deletion must stay visible after the profile data changes.
                state.update { it.copy(account = account, isLoading = false, isRefreshing = false) }
            }
        }
    }

    fun onEditName() = navigationContext.tryNavigate(ProfileFlow.Name)

    /** Signing out or deleting the account sends the app back to the tabs (the splash watches sign-in). */
    fun onSignOut() = viewModelScope.launch(unexpectedExceptionHandler) {
        useCase.signOut()
    }

    fun onDeleteAccount() {
        if (state.value.isDeleting) return

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isDeleting = true, error = null) }

            useCase.deleteAccount()

            state.update { it.copy(isDeleting = false) }
        }
    }

    fun onDismissError() = state.update { it.copy(error = null) }
}
