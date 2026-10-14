package com.ugg.client.kit.profile.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.ugg.client.kit.navigation.domain.external.contexts.NavigationContext
import com.ugg.client.kit.profile.domain.entities.FootSize
import com.ugg.client.kit.profile.domain.states.SizeState
import com.ugg.client.kit.profile.domain.useCases.SizeUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** «24,5» at most: digits and one comma or dot. */
private const val FOOT_LENGTH_MAX_CHARS = 4

class SizeViewModel(
    private val useCase: SizeUseCase,
    private val navigationContext: NavigationContext,
) : ViewModel() {
    val state: StateFlow<SizeState>
        field = MutableStateFlow(SizeState(isLoading = true))

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@SizeViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false, isSaving = false) }
    }

    /** Called by the screen each time it opens, and by «Повторить». */
    fun load() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        val (options, current) = coroutineScope {
            val options = async { useCase.options() }
            val current = async { useCase.current() }
            options.await() to current.await()
        }

        state.update {
            it.copy(
                options = options,
                size = current.size,
                heightId = current.heightId ?: options.heights.firstOrNull()?.id,
                footLength = current.footLengthCm?.toInput().orEmpty(),
                isLoading = false,
            )
        }
    }

    /** Tapping the chosen size again clears it. */
    fun onSelectSize(size: String) = state.update { it.copy(size = size.takeIf { _ -> size != it.size }, error = null) }

    fun onSelectHeight(id: String) = state.update { it.copy(heightId = id, error = null) }

    fun onFootLengthChange(input: String) {
        var separatorSeen = false
        val cleaned = input.filter { char ->
            when {
                char.isDigit() -> true
                (char == ',' || char == '.') && !separatorSeen -> { separatorSeen = true; true }
                else -> false
            }
        }.replace('.', ',').take(FOOT_LENGTH_MAX_CHARS)

        state.update { it.copy(footLength = cleaned, error = null) }
    }

    fun onSave() {
        val current = state.value
        if (!current.canSave) return

        viewModelScope.launch(unexpectedExceptionHandler) {
            state.update { it.copy(isSaving = true, error = null) }

            useCase.save(FootSize(size = current.size, heightId = current.heightId, footLengthCm = current.footLengthCm))

            state.update { it.copy(isSaving = false) }
            navigationContext.back()
        }
    }
}

/** 24.5 → «24,5», 25.0 → «25». */
private fun Double.toInput(): String {
    val tenths = kotlin.math.round(this * 10).toLong()
    return if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10},${tenths % 10}"
}
