package com.ugg.client.kit.profile.domain.states

import com.ugg.client.kit.profile.domain.entities.Account

data class SettingsState(
    val account: Account? = null,
    /** «Версия 1.0 (1)». */
    val version: String = "",
    val isLoading: Boolean = false,
    /** Pull-to-refresh in progress: the content stays on screen under the spinner. */
    val isRefreshing: Boolean = false,
    val isDeleting: Boolean = false,
    val error: Throwable? = null,
)
