# Architecture Migration Design: PayBuddy → template-client

**Date:** 2026-04-02
**Status:** Approved

---

## Overview

Migrate the KMM/CMP architecture from `paybuddy-client` into the empty `template-client` template. Only `core` and `navigation` feature modules are ported; a stub `auth` feature is added as a sample implementation. Targets: Android + iOS only.

---

## Module Structure

```
template-client/
├── build-logic/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── AndroidConfig.kt
│       ├── setup-android-application.gradle.kts
│       └── setup-multiplatform-library.gradle.kts   # Android + iOS only (no wasmJs)
│
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties             # Gradle 9.4.0
│
├── shared/
│   └── src/commonMain/kotlin/digital/factorsmile/client/shared/
│       ├── AppDI.kt
│       └── ContentView.kt
│
├── androidApp/                                        # replaces :app
│   └── src/main/kotlin/digital/factorsmile/client/android/
│       └── MainActivity.kt
│
├── client-kit/
│   ├── core/
│   │   ├── data/build.gradle.kts
│   │   ├── domain/build.gradle.kts
│   │   └── presentation/
│   │       ├── build.gradle.kts
│   │       └── src/commonMain/.../core/presentation/external/CoreModule.kt
│   │
│   ├── navigation/
│   │   ├── data/build.gradle.kts
│   │   ├── domain/
│   │   │   ├── build.gradle.kts
│   │   │   └── src/commonMain/.../navigation/domain/external/
│   │   │       ├── contexts/NavigationContext.kt
│   │   │       └── entities/NavigationEvent.kt
│   │   └── presentation/
│   │       ├── build.gradle.kts
│   │       └── src/commonMain/.../navigation/presentation/external/
│   │           ├── NavigationModule.kt
│   │           └── view/NavigationScreen.kt
│   │
│   └── auth/                                          # new sample feature
│       ├── data/
│       │   ├── build.gradle.kts
│       │   └── src/commonMain/.../auth/data/
│       │       └── useCases/StubAuthUseCase.kt
│       ├── domain/
│       │   ├── build.gradle.kts
│       │   └── src/commonMain/.../auth/domain/
│       │       ├── entities/AuthCredentials.kt
│       │       ├── useCases/AuthUseCase.kt (interface)
│       │       └── states/AuthDestination.kt
│       └── presentation/
│           ├── build.gradle.kts
│           └── src/commonMain/.../auth/presentation/
│               ├── external/
│               │   ├── AuthModule.kt
│               │   └── view/
│               │       ├── AuthScreen.kt
│               │       └── models/AuthViewModel.kt
│               └── internal/view/AuthView.kt
│
├── .gitignore                                         # copied from PayBuddy
├── settings.gradle.kts
├── build.gradle.kts
└── gradle.properties
```

---

## Naming Replacements

| PayBuddy | template-client |
|---|---|
| `cloud.paybuddy` (package) | `com.template` |
| `PayBuddyCore` | `DsCore` |
| `PayBuddyApi` | `DsApi` |
| `paybuddy-shared-core` lib | `com.template:client-api-core:1.0.0` |
| `paybuddy-ui-foundation` | removed — use standard `MaterialTheme` |
| `paybuddy-client-shared-*` | removed |
| `multiplatform-foundation-*` | kept |
| `PayBuddyShared` (XCFramework) | `TemplateShared` |
| SKIE plugin | kept |
| `wasmJs` target | removed |
| GitHub Packages | GitLab Package Registry |

---

## Key Configuration Changes

### build-logic/setup-multiplatform-library.gradle.kts
- Remove `wasmJs` target block
- Remove `webMain` source set
- Keep: Android, iosX64, iosArm64, iosSimulatorArm64
- Keep SKIE in shared module

### gradle/libs.versions.toml
- Replace `paybuddy-shared-core` → `template-client-api-core = { group = "com.template", name = "client-api-core", version = "1.0.0" }`
- Remove `paybuddy-ui-foundation`, `paybuddy-client-shared-catalog`
- Keep `multiplatform-foundation-core`, `multiplatform-foundation-network`
- Keep SKIE
- Remove `paybuddy-ui-version`, `paybuddy-client-shared-version`
- Add `template-client-api-version = "1.0.0"`

### Root build.gradle.kts
- `group = "com.template"`
- Replace GitHub Packages with GitLab Package Registry:
  ```kotlin
  maven {
      name = "GitLabPackages"
      url = uri("https://gitlab.com/api/v4/groups/factorsmile/-/packages/maven")
      credentials {
          username = System.getenv("GITLAB_USER")
          password = System.getenv("GITLAB_TOKEN")
      }
  }
  ```

### shared/build.gradle.kts
- Remove scanners and catalog from exported modules (only core + navigation + auth)
- XCFramework name: `TemplateShared`
- `namespace = "com.template.client.shared"`

### androidApp/build.gradle.kts
- `applicationId = "com.template.client.android"`
- `namespace = "com.template.client.android"`

### core/presentation/CoreModule.kt
```kotlin
package com.template.client.kit.core.presentation.external

import com.template.client.api.core.DsCore
import com.template.client.api.core.dsl.DsApi
import io.ktor.client.plugins.logging.LogLevel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val coreModule: DI.Module
    get() = DI.Module("fs-core") {
        bind<DsCore>() with singleton {
            DsCore {
                api = DsApi.Client.Localhost
                logLevel = LogLevel.INFO
            }
        }
    }
```

---

## Auth Feature (Stub)

### Domain
- `AuthCredentials(login: String, password: String)`
- `AuthUseCase` interface: `suspend fun login(credentials: AuthCredentials): Boolean`
- `AuthDestination` sealed class (Serializable): `Login`

### Data
- `StubAuthUseCase`: hardcoded `login == "user" && password == "password"` → returns `true`

### Presentation
- `AuthViewModel`: holds `login`/`password` state, calls `AuthUseCase.login()`, emits success/error
- `AuthScreen`: public composable, entry point
- `AuthView`: internal login form UI (TextField × 2, Button)

### Navigation integration
- `NavigationScreen` starts at `AuthDestination.Login`
- On success → navigates to a stub home/placeholder screen

---

## settings.gradle.kts Module Registration

```kotlin
listOf(
    Module(":client-kit:core-data",              "client-kit/core/data"),
    Module(":client-kit:core-domain",            "client-kit/core/domain"),
    Module(":client-kit:core-presentation",      "client-kit/core/presentation"),

    Module(":client-kit:navigation-data",        "client-kit/navigation/data"),
    Module(":client-kit:navigation-domain",      "client-kit/navigation/domain"),
    Module(":client-kit:navigation-presentation","client-kit/navigation/presentation"),

    Module(":client-kit:auth-data",              "client-kit/auth/data"),
    Module(":client-kit:auth-domain",            "client-kit/auth/domain"),
    Module(":client-kit:auth-presentation",      "client-kit/auth/presentation"),
)
```

---

## What Is NOT Migrated

- `catalog` feature (PayBuddy-specific)
- `scanners` feature (PayBuddy-specific)
- `webApp` module
- `wasmJs` target
