package com.ugg.client.infrastructure.keystore.foundation.external

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.touchlab.kermit.Logger

/**
 * `EncryptedSharedPreferences`-backed [Keystore].
 *
 * **Accessibility is not supported on this platform.** `EncryptedSharedPreferences` has no
 * per-entry protection class: entries are readable whenever the process can reach its master key,
 * with no equivalent of `kSecAttrAccessibleWhenUnlocked` or `AfterFirstUnlock`. Rather than pretend
 * the requested level was applied, every successful write reports
 * [KeystoreWriteResult.StoredWithoutAccessibility] carrying the level that was asked for.
 *
 * If encrypted storage cannot be created, the store stays unusable and every operation reports
 * [KeystoreFailure.EncryptionUnavailable] — unless
 * [KeystoreConfiguration.allowUnencryptedFallback] is explicitly turned on.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateKeystore actual constructor(
    actual override val configuration: KeystoreConfiguration,
) : Keystore {

    private var preferences: SharedPreferences? = null
    private var failure: KeystoreFailure? = null

    init {
        val context = configuration.context

        if (context == null) {
            failure = KeystoreFailure.NotConfigured
        } else {
            try {
                val key = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                preferences = EncryptedSharedPreferences.create(
                    context,
                    configuration.filename,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (exception: Exception) {
                if (configuration.allowUnencryptedFallback) {
                    Logger.e(exception) {
                        "Encrypted storage unavailable; falling back to PLAINTEXT SharedPreferences " +
                            "because allowUnencryptedFallback is enabled."
                    }
                    preferences = context.getSharedPreferences(
                        configuration.filename,
                        Context.MODE_PRIVATE,
                    )
                } else {
                    Logger.e(exception) { "Encrypted storage unavailable; the keystore is disabled." }
                    failure = KeystoreFailure.EncryptionUnavailable(exception.message)
                }
            }
        }
    }

    actual override fun get(key: String): KeystoreReadResult {
        val preferences = preferences
            ?: return KeystoreReadResult.Failure(failure ?: KeystoreFailure.NotConfigured)

        return try {
            preferences.getString(key, null)
                ?.let { KeystoreReadResult.Found(it) }
                ?: KeystoreReadResult.NotFound
        } catch (exception: Exception) {
            KeystoreReadResult.Failure(KeystoreFailure.Unknown(exception.message))
        }
    }

    actual override fun set(
        key: String,
        value: String?,
        accessibility: KeystoreAccessibility,
    ): KeystoreWriteResult {
        val preferences = preferences
            ?: return KeystoreWriteResult.Failure(failure ?: KeystoreFailure.NotConfigured)

        return try {
            val editor = preferences.edit()

            if (value != null) editor.putString(key, value) else editor.remove(key)

            if (editor.commit()) {
                KeystoreWriteResult.StoredWithoutAccessibility(accessibility)
            } else {
                KeystoreWriteResult.Failure(
                    KeystoreFailure.Unknown("SharedPreferences.commit() failed for key $key")
                )
            }
        } catch (exception: Exception) {
            KeystoreWriteResult.Failure(KeystoreFailure.Unknown(exception.message))
        }
    }
}
