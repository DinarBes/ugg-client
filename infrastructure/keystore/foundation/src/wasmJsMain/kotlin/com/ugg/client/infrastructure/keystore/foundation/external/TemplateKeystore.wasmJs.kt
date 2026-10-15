package com.ugg.client.infrastructure.keystore.foundation.external

import kotlinx.browser.localStorage

/**
 * `localStorage`-backed [Keystore] for the web client.
 *
 * The browser has no secure enclave: anything here is readable by scripts of the same site, so
 * it holds only small, low-value flags (onboarding seen, cached session tokens of the template).
 * Firebase keeps its own session in IndexedDB. Disabled storage (private mode in some browsers)
 * is reported as a failure, never thrown.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateKeystore actual constructor(
    actual override val configuration: KeystoreConfiguration,
) : Keystore {

    actual override fun get(key: String): KeystoreReadResult = try {
        localStorage.getItem(configuration.prefix + key)
            ?.let { KeystoreReadResult.Found(it) }
            ?: KeystoreReadResult.NotFound
    } catch (e: Throwable) {
        KeystoreReadResult.Failure(KeystoreFailure.Unknown(e.message))
    }

    actual override fun set(
        key: String,
        value: String?,
        accessibility: KeystoreAccessibility,
    ): KeystoreWriteResult = try {
        if (value == null) {
            localStorage.removeItem(configuration.prefix + key)
        } else {
            localStorage.setItem(configuration.prefix + key, value)
        }
        KeystoreWriteResult.StoredWithoutAccessibility(accessibility)
    } catch (e: Throwable) {
        KeystoreWriteResult.Failure(KeystoreFailure.Unknown(e.message))
    }
}
