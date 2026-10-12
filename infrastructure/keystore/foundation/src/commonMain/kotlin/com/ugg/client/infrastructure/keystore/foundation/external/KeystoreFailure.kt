package com.ugg.client.infrastructure.keystore.foundation.external

/** Why an operation could not be carried out. Diagnostic detail, never a control-flow signal. */
sealed interface KeystoreFailure {
    /** Raw platform status code — `OSStatus` on Apple platforms. */
    data class PlatformStatus(val status: Long) : KeystoreFailure

    /** Encrypted storage could not be created and no plaintext fallback was permitted. */
    data class EncryptionUnavailable(val message: String?) : KeystoreFailure

    /** The keystore is missing something it needs — an Android `Context`, typically. */
    data object NotConfigured : KeystoreFailure

    data class Unknown(val message: String?) : KeystoreFailure
}
