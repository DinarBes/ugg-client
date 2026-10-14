package com.ugg.client.kit.splash.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.splash.domain.useCases.SplashUseCase
import com.ugg.client.kit.splash.presentation.external.states.SplashState
import com.ugg.client.kit.splash.domain.entities.SplashFlow
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

@Suppress("unused")
fun DI.splashViewModel(): SplashViewModel = this.direct.instance()

/**
 * Owned by the navigation host, not by the splash entry: it keeps listening for authorization
 * events after the splash destination leaves the back stack.
 */
class SplashViewModel(
    private val splashUseCase: SplashUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<SplashState>
        field = MutableStateFlow(SplashState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@SplashViewModel::class.toString() }

        // KeystoreUnavailableException lands here: no navigation happens, the splash stays put and
        // shows the reason instead of spinning forever or guessing that the user is signed out.
        state.update { it.copy(error = throwable) }
    }

    init {
        fetch()
        observe()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(error = null) }

        navigationContext.navigate(splashUseCase.getInitialFlow()) { clear() }
    }

    private fun observe() = viewModelScope.launch(unexpectedExceptionHandler) {
        splashUseCase.flows().collect { flow ->
            navigationContext.navigate(flow) {
                clear()
                // The name step sits over the app, so «back» on it simply skips it.
                if (flow == SplashFlow.Registration) add(SplashFlow.Launch)
            }
        }
    }
}
