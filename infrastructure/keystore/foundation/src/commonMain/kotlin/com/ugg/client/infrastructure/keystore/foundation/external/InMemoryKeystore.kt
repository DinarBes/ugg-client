package com.ugg.client.infrastructure.keystore.foundation.external

/**
 * Non-persistent [Keystore] for tests and `AppEnvironment.MOCK`.
 *
 * Doubles as the reference implementation of the contract: it records the accessibility of every
 * entry and re-applies it on rewrite, so contract tests can assert the level actually changes.
 */
class InMemoryKeystore(
    override val configuration: KeystoreConfiguration = KeystoreConfiguration(),
) : Keystore {
    private data class Entry(val value: String, val accessibility: KeystoreAccessibility)

    private val entries: MutableMap<String, Entry> = mutableMapOf()

    override fun get(key: String): KeystoreReadResult =
        entries[key]?.let { KeystoreReadResult.Found(it.value) } ?: KeystoreReadResult.NotFound

    override fun set(
        key: String,
        value: String?,
        accessibility: KeystoreAccessibility,
    ): KeystoreWriteResult {
        if (value == null) entries.remove(key) else entries[key] = Entry(value, accessibility)

        return KeystoreWriteResult.Stored
    }

    /** Accessibility currently recorded for [key], or `null` when there is no entry. */
    fun accessibilityOf(key: String): KeystoreAccessibility? = entries[key]?.accessibility
}
