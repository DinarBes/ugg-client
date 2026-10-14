package com.ugg.client.kit.profile.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.states.ProfileState
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ProfileViewModel(
    private val useCase: ProfileUseCase,
) : ViewModel() {
    val state: StateFlow<ProfileState>
        field = MutableStateFlow(ProfileState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@ProfileViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    private var observing: Job? = null

    init {
        observe()
    }

    /** (Re)subscribes to the profile; «Повторить» after an error lands here. */
    fun observe() {
        observing?.cancel()
        observing = viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isLoading = true, error = null) }

            useCase.observeProfile().collect(::show)
        }
    }

    fun signOut() = viewModelScope.launch(unexpectedExceptionHandler) {
        useCase.signOut()
    }

    private fun show(profile: Profile?) = state.update {
        it.copy(profile = profile, isGuest = profile == null, isLoading = false, error = null)
    }
}

@Suppress("unused")
fun DI.profileViewModel(): ProfileViewModel = this.direct.instance()
