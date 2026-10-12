package com.ugg.client.infrastructure.keystore.foundation.external

/**
 * Tuning knobs for [TemplateKeystore].
 *
 * Platform-specific fields live on the `actual` declarations — Android adds `context`, `filename`
 * and `allowUnencryptedFallback`; Apple adds `accessGroup` and `service`.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class KeystoreConfiguration() {
    /** Applied to entries written without an explicit level. See [Keystore.set]. */
    var defaultAccessibility: KeystoreAccessibility
}
