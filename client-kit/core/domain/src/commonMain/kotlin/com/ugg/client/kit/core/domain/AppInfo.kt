package com.ugg.client.kit.core.domain

/** The installed build, as the platform app reports it; shown at the bottom of the settings. */
data class AppInfo(
    val versionName: String = "",
    val versionCode: Int = 0,
) {
    /** «Версия 1.0 (1)»; empty when the host did not say. */
    val label: String
        get() = when {
            versionName.isEmpty() -> ""
            versionCode > 0 -> "Версия $versionName ($versionCode)"
            else -> "Версия $versionName"
        }
}
