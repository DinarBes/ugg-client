# Mock Keystore Override — Design Spec

**Date:** 2026-06-13
**Status:** Approved

## Problem

`TemplateCoreConfiguration` does not expose a `keystore` field. As a result, `MultiplatformCoreModule` always installs the platform-native keystore (Android Keystore / iOS Keychain). Infrastructure tests cannot substitute a lightweight in-memory keystore, which makes them platform-dependent and harder to run in CI.

The working fork (`fs-client`) already solves this; the template needs to be brought to parity.

## Solution

Add an optional `keystore: Keystore?` field to `TemplateCoreConfiguration`. When the field is non-null, `MultiplatformCoreModule` forwards it to `KeystorePlugin`; when null, the platform default is used unchanged.

## Changed Files

### `infrastructure/api/core/…/TemplateCoreConfiguration.kt`

Add field:

```kotlin
var keystore: Keystore? = null
```

### `infrastructure/api/core/…/multiplatform/MultiplatformCoreModule.kt`

Update `KeystorePlugin` installation:

```kotlin
install(KeystorePlugin) {
    if (configuration.keystore != null)
        keystore = configuration.keystore
}
```

## What Is NOT Changed

- `InMemoryKeystore` and `MockKeystore` — already present in `infrastructure/api/auth`.
- `TemplateCore`, `TemplateAuth`, `KeystoreModule` — no changes needed; they consume `MultiplatformCore` which already wraps the keystore correctly after this fix.
- No new files, no new modules.

## Usage After This Change

Tests that build `TemplateCore` can inject an `InMemoryKeystore`:

```kotlin
val core = TemplateCore {
    keystore = InMemoryKeystore()
    api = TemplateApi.Client.Development
}
```

## Out of Scope

- `engine` override (separate feature, not requested)
- `exceptionsConverter` override (separate feature)
- Expect/actual split of `TemplateCoreConfiguration` (fs-client does this for Android context; the template has no platform-specific setup that requires it)
