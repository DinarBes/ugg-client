package com.ugg.client.infrastructure.keystore.foundation.internal

import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreFailure
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreReadResult
import com.ugg.client.infrastructure.keystore.foundation.external.KeystoreWriteResult
import platform.Security.errSecInteractionNotAllowed
import platform.Security.errSecItemNotFound

/**
 * Translates an `OSStatus` into a read outcome.
 *
 * `errSecInteractionNotAllowed` is the whole reason this module exists: the previous implementation
 * lumped it in with genuine errors and threw, which on Kotlin/Native means `abort()`.
 */
internal fun Int.asReadFailure(): KeystoreReadResult = when (this) {
    errSecItemNotFound -> KeystoreReadResult.NotFound
    errSecInteractionNotAllowed -> KeystoreReadResult.TemporarilyUnavailable
    else -> KeystoreReadResult.Failure(KeystoreFailure.PlatformStatus(this.toLong()))
}

internal fun Int.asWriteFailure(): KeystoreWriteResult = when (this) {
    errSecInteractionNotAllowed -> KeystoreWriteResult.TemporarilyUnavailable
    else -> KeystoreWriteResult.Failure(KeystoreFailure.PlatformStatus(this.toLong()))
}
