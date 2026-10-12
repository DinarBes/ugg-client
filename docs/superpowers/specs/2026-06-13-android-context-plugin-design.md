# Android Context Plugin — Design Spec

**Date:** 2026-06-13
**Status:** Approved

## Problem

`TemplateCoreConfiguration` is a plain `common` class. `AndroidContextPlugin` is never installed, so `MultiplatformCore` has no Android `Context`. On Android, certain `multiplatform-foundation` features — notably the native Keystore (Encrypted SharedPreferences) — require a `Context` to initialise. This matches the gap already solved in `fs-client`.

## Solution

Convert `TemplateCoreConfiguration` to an `expect/actual` class. The Android `actual` adds an `androidContext: Context?` field and installs `AndroidContextPlugin` in a platform-specific `multiplatformSetup()` extension. The Apple `actual` has an empty `multiplatformSetup()`. `MultiplatformCoreModule` calls `multiplatformSetup()` on the configuration before installing other plugins. `MainActivity` passes `androidContext = this@MainActivity`.

`AndroidContextPlugin` is already on the classpath via `multiplatform-foundation-core`, which is declared in the `setup-multiplatform-infrastructure` convention plugin.

## Changed Files

### New: `infrastructure/api/core/src/androidMain/kotlin/…/TemplateCoreConfiguration.android.kt`

```kotlin
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateCoreConfiguration {
    var androidContext: Context? = null
    actual var api: TemplateApi = TemplateApi.Client.Localhost
    actual var logLevel: LogLevel = LogLevel.INFO
    actual var keystore: Keystore? = null
    actual fun MultiplatformCoreBuilder.multiplatformSetup() {
        if (androidContext != null)
            install(AndroidContextPlugin) { context = androidContext }
    }
}
```

### New: `infrastructure/api/core/src/appleMain/kotlin/…/TemplateCoreConfiguration.apple.kt`

```kotlin
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateCoreConfiguration {
    actual var api: TemplateApi = TemplateApi.Client.Localhost
    actual var logLevel: LogLevel = LogLevel.INFO
    actual var keystore: Keystore? = null
    actual fun MultiplatformCoreBuilder.multiplatformSetup() { }
}
```

### Modify: `infrastructure/api/core/src/commonMain/kotlin/…/TemplateCoreConfiguration.kt`

Replace with `expect` declaration:

```kotlin
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TemplateCoreConfiguration() {
    var api: TemplateApi
    var logLevel: LogLevel
    var keystore: Keystore?
    fun MultiplatformCoreBuilder.multiplatformSetup()
}
```

### Modify: `infrastructure/api/core/src/commonMain/kotlin/…/MultiplatformCoreModule.kt`

Add `with(configuration) { multiplatformSetup() }` as the first call inside `MultiplatformCore { … }`:

```kotlin
MultiplatformCore {
    with(configuration) { multiplatformSetup() }

    install(KeystorePlugin) {
        if (configuration.keystore != null)
            keystore = configuration.keystore
    }
    // … rest unchanged
}
```

### Modify: `androidApp/src/main/kotlin/…/MainActivity.kt`

Add `androidContext = this@MainActivity` inside the `TemplateCore { … }` block.

## What Is NOT Changed

- `KeystoreOverrideTest` — uses the `expect` API (`keystore` field) which exists on all platforms; no changes needed.
- Convention plugin (`setup-multiplatform-infrastructure.gradle.kts`) — already declares `androidMain` and `appleMain` targets; source set directories just need to be created.
- No new Gradle dependencies — `AndroidContextPlugin` is in `multiplatform-foundation-core` already on the classpath.

## Package Path

Full package for new files:
`com.template.client.infrastructure.api.core`
