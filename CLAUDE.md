# Template Client — CLAUDE.md

## Project Overview

Kotlin Multiplatform Mobile (KMM) template application. Targets Android and iOS via Compose Multiplatform. Stub implementations ready for backend integration.

- **Organization**: `com.ugg`
- **Version**: 1.0.0
- **Kotlin**: 2.3.10 / JVM 21
- **Min Android SDK**: 24, Compile/Target SDK: 36

## Build Commands

```bash
# Android
./gradlew :androidApp:build
./gradlew :androidApp:installDebug

# Tests (all modules) — there is no aggregate `test` task; name the real ones
./gradlew iosSimulatorArm64Test testAndroidHostTest

# Tests (specific module)
./gradlew :client-kit:auth-presentation:iosSimulatorArm64Test

# iOS XCFramework
./gradlew :shared:assembleUggSharedXCFramework
```

> `testAndroidHostTest` currently fails in modules that construct `MultiplatformCore`: the
> `multiplatform-foundation` JVM artifacts are compiled for a newer JVM than the project's
> toolchain (`UnsupportedClassVersionError`, class file version 69 vs 65). Unrelated to test
> content — the same tests pass on `iosSimulatorArm64Test`.

## Module Structure

```
ugg-client/
├── androidApp/          # Android entry point (MainActivity)
├── shared/              # KMP shared library → exports UggShared.xcframework for iOS
│   └── AppDI.kt         # Root DI composition
│   └── ContentView.kt   # Root composable with withDI wrapper
├── infrastructure/      # HTTP client + secure storage foundation
│   ├── api/{core,auth}/  # api-auth takes tokens via a builder block — no keystore dep
│   └── keystore/
│       ├── foundation/   # generic secure storage
│       └── session/      # AccessService: keystore-backed token store + AuthorizationStatus
├── client-kit/          # Feature modules
│   ├── core/{data,domain,presentation}/
│   ├── auth/{data,domain,presentation}/
│   └── navigation/{data,domain,presentation}/
└── build-logic/         # Convention plugins (DRY gradle config)
```

## Architecture

**Clean Architecture** with strict 3-layer separation per feature:

| Layer | Responsibility | Rule |
|-------|---------------|------|
| `domain` | Interfaces, entities, sealed states | No framework dependencies |
| `data` | Interface implementations | Depends on domain only |
| `presentation` | Compose UI, ViewModels, DI modules | Depends on data + domain |

Dependencies always flow inward: `presentation → data → domain`.

**Package convention:**
- `external/` — public API surface
- `internal/` — implementation details (not exported)

## Key Technologies

| Concern | Library |
|---------|---------|
| UI | Compose Multiplatform 1.10.2 + Material3 |
| DI | Kodein DI 7.31.0 |
| Navigation | AndroidX Navigation Compose 2.9.2 |
| State | Kotlin StateFlow / SharedFlow + Coroutines 1.10.2 |
| Serialization | Kotlinx Serialization 1.8.1 |
| Logging | Kermit 2.1.0 |
| iOS interop | SKIE 0.10.10 |
| Date/Time | Kotlinx DateTime 0.7.1 |
| Testing | Kotlin Test + Coroutines Test |

External dependencies:
- `ugg-client-api-core:1.0.0` — API client (replace with your own)
- `multiplatform-foundation:1.3.1` (cloud.paybuddy) — core and network modules

## Dependency Injection

Kodein DI with Compose integration. Each feature defines its own `XyzModule`:

```kotlin
// shared/AppDI.kt
val appDI = DI {
    import(coreModule)
    import(authModule)
    import(navigationModule)
}
```

UI wrapped with `withDI(appDI)` in `ContentView`. Resolved via `localDI()` and `instance<T>()`.

## Infrastructure Layer

`infrastructure/api-core` and `infrastructure/api-auth` provide the HTTP client foundation.

**`TemplateCore`** — Ktor `HttpClient` + serialization. Configured in `TemplateAppBuilder`:
```kotlin
var core = TemplateCore {
    api = TemplateApi.Client.Localhost  // change to your server URL
    logLevel = LogLevel.INFO
}
```

**`TemplateAuth`** — extends core DI with bearer token auth. It takes its tokens through a builder
block and has **no dependency on the keystore** — the API layer never touches secure storage. The
composition root (`TemplateApp`) supplies the token source:

```kotlin
bind<TemplateAuth>() with singleton {
    val access: AccessService = instance()      // keystore-backed, from :infrastructure:keystore-session
    TemplateAuth(instance()) {
        bearerAuthConfig {
            loadTokens {
                val a = access.accessTokenOrNull() ?: return@loadTokens null
                val r = access.refreshTokenOrNull() ?: return@loadTokens null
                BearerTokens(a, r)
            }
            refreshTokens { /* call refresh, access.store(...), or access.clear() + null */ }
        }
    }
}
```

This is the key layering rule: **tokens flow into `api-auth` through the `bearerAuthConfig` block,
never through a direct keystore dependency.** `api-core` and `api-auth` stay free of
`:infrastructure:keystore-foundation`; only the composition root and `:infrastructure:keystore-session`
know about storage.

**Adding a new API service** (e.g., `infrastructure/api-profile`):
1. Create module with `setup-multiplatform-infrastructure` plugin
2. Add `implementation(projects.infrastructure.apiAuth)` dep
3. Create `ProfileService(auth: TemplateAuth)` — use `auth.client` for HTTP calls
4. Bind the service in the relevant feature `DI.Module`

### Keystore & session tokens

Two modules, kept apart on purpose:

- **`infrastructure/keystore/foundation`** (`:infrastructure:keystore-foundation`) — generic secure
  storage (Keychain / `EncryptedSharedPreferences`). Knows nothing about tokens.
- **`infrastructure/keystore/session`** (`:infrastructure:keystore-session`) — `AccessService`, the
  app's token store. It maps the two token keys
  onto the keystore and derives `AuthorizationStatus`. This is the *only* consumer of the keystore
  besides the composition root, and it plays the role a `FirebaseAuthService` plays in a
  Firebase-backed app: the token source that the composition root feeds into `TemplateAuth`'s
  `bearerAuthConfig` block.

`keystore-foundation` uses `setup-multiplatform-library`, **not**
`setup-multiplatform-infrastructure`: the latter pulls in `multiplatform-foundation-keystore`, the
library this module replaces.

The keystore is chosen at composition on `TemplateAppBuilder` (not on `TemplateCore`):

```kotlin
TemplateApp {
    keystore { context = androidContext }   // default TemplateKeystore; Apple needs no context
    // or, for tests / AppEnvironment.MOCK:
    keystore = InMemoryKeystore()
}
```

**Neither operation throws.** On Kotlin/Native an undeclared exception crossing the Objective-C
boundary is an unconditional `abort()` that no Swift `do/catch` can intercept, so outcomes are
values:

```kotlin
when (val result = keystore.get("refresh-token")) {
    is KeystoreReadResult.Found -> result.value
    KeystoreReadResult.NotFound -> null              // no token — the user is signed out
    KeystoreReadResult.TemporarilyUnavailable -> …   // unreadable — NOT a signed-out user
    is KeystoreReadResult.Failure -> …               // result.reason carries the OSStatus
}
```

`NotFound` and `TemporarilyUnavailable` must stay distinct. The device may not have been unlocked
since boot when iOS launches the app in the background, and treating "cannot read" as "not signed
in" signs out a valid user. `AccessService.authorizationStatus()` (in `:infrastructure:keystore-session`)
returns `Authorized` / `NotAuthorized` / `Undetermined` for exactly this reason; `SharedSplashUseCase`
turns `Undetermined` into a failed `Result` so the splash screen stays put instead of guessing.

**Accessibility is per entry**, chosen from `KeystoreAccessibility` rather than raw
`kSecAttrAccessible*` constants — the mapping lives in `appleMain`. The default is
`AfterFirstUnlockThisDeviceOnly`, which is what keeps tokens readable during a locked-screen
background launch. Secrets only needed with the user present can stay on stricter levels:

```kotlin
keystore.set("pin-hash", hash, KeystoreAccessibility.WhenPasscodeSetThisDeviceOnly)
```

Changing the level of an existing entry applies it: `kSecAttrAccessible` is written in both the
`SecItemAdd` query and the `SecItemUpdate` attribute dictionary. Setting it only on add would leave
an existing entry on its original protection class forever.

**Android has no per-entry protection class.** Rather than pretend otherwise, a successful write
there returns `KeystoreWriteResult.StoredWithoutAccessibility(requested)`. Use `.isStored` when you
only care that the value reached storage. If `EncryptedSharedPreferences` cannot be created the
store stays unusable and reports `KeystoreFailure.EncryptionUnavailable` — it never silently falls
back to plaintext unless `allowUnencryptedFallback` is explicitly enabled.

Swap the implementation via `TemplateApp { keystore = … }`; `InMemoryKeystore` is the fake for
tests and `AppEnvironment.MOCK`.

## State Management Pattern

ViewModels use `StateFlow` for UI state. `NavigationContext` uses `SharedFlow` as a typed event bus. Navigation events are sealed classes (`NavigationEvent.OnNavigate<T>`).

## Testing

Tests live in `commonTest/` alongside each feature module. Use `StandardTestDispatcher` and `advanceUntilIdle()` / `advanceTimeBy()` for coroutine control. Prefer fake implementations (inner classes) over mocks.

```bash
# Run with output
./gradlew :client-kit:auth-presentation:iosSimulatorArm64Test --info
```

## Adding a New Feature Module

When asked to create a feature module (e.g. "add a `profile` module"), follow these steps in order.
Notation used below: `<feature>` = lowercase name (e.g. `profile`), `<Feature>` = capitalized (e.g. `Profile`), `<pkg>` = root package from `androidApp/build.gradle.kts` minus `.android` (e.g. `com.ugg.client`).

### Step 1 — Create 12 files

#### domain layer

**`client-kit/<feature>/domain/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "<pkg>.kit.<feature>.domain"
    }
}
```

**`client-kit/<feature>/domain/src/commonMain/kotlin/<pkg>/kit/<feature>/domain/entities/<Feature>Entity.kt`**
```kotlin
package <pkg>.kit.<feature>.domain.entities

data class <Feature>Entity(
    val id: String
)
```

**`client-kit/<feature>/domain/src/commonMain/kotlin/<pkg>/kit/<feature>/domain/useCases/<Feature>UseCase.kt`**
```kotlin
package <pkg>.kit.<feature>.domain.useCases

import <pkg>.kit.<feature>.domain.entities.<Feature>Entity

interface <Feature>UseCase {
    suspend fun get(): <Feature>Entity
}
```

**`client-kit/<feature>/domain/src/commonMain/kotlin/<pkg>/kit/<feature>/domain/states/<Feature>State.kt`**
```kotlin
package <pkg>.kit.<feature>.domain.states

import <pkg>.kit.<feature>.domain.entities.<Feature>Entity

data class <Feature>State(
    val entity: <Feature>Entity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

#### data layer

**`client-kit/<feature>/data/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "<pkg>.kit.<feature>.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.<feature>Domain)
        }
    }
}
```

**`client-kit/<feature>/data/src/commonMain/kotlin/<pkg>/kit/<feature>/data/useCases/Stub<Feature>UseCase.kt`**
```kotlin
package <pkg>.kit.<feature>.data.useCases

import <pkg>.kit.<feature>.domain.entities.<Feature>Entity
import <pkg>.kit.<feature>.domain.useCases.<Feature>UseCase

class Stub<Feature>UseCase : <Feature>UseCase {
    override suspend fun get(): <Feature>Entity {
        return <Feature>Entity(id = "stub")
    }
}
```

#### presentation layer

**`client-kit/<feature>/presentation/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "<pkg>.kit.<feature>.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.<feature>Data)
            implementation(projects.clientKit.<feature>Domain)
        }
    }
}
```

**`…/presentation/src/commonMain/kotlin/<pkg>/kit/<feature>/presentation/external/<Feature>Module.kt`**
```kotlin
package <pkg>.kit.<feature>.presentation.external

import <pkg>.kit.<feature>.data.useCases.Shared<Feature>UseCase
import <pkg>.kit.<feature>.data.useCases.Stub<Feature>UseCase
import <pkg>.kit.<feature>.domain.useCases.<Feature>UseCase
import <pkg>.kit.<feature>.presentation.external.view.models.<Feature>ViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

val <feature>Module: DI.Module
    get() = DI.Module("<feature>.module") {
        bind<<Feature>UseCase>() with factory {
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> Shared<Feature>UseCase()
                AppEnvironment.MOCK -> Stub<Feature>UseCase()
            }
        }
        bind<<Feature>ViewModel>() with factory {
            <Feature>ViewModel(instance())
        }
    }
```

**`…/presentation/external/view/models/<Feature>ViewModel.kt`**
```kotlin
package <pkg>.kit.<feature>.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import <pkg>.kit.<feature>.domain.states.<Feature>State
import <pkg>.kit.<feature>.domain.useCases.<Feature>UseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class <Feature>ViewModel(private val useCase: <Feature>UseCase) : ViewModel() {

    val state: StateFlow<<Feature>State>
        field = MutableStateFlow(<Feature>State())

    fun load() = viewModelScope.launch {
        state.update { it.copy(isLoading = true, error = null) }
        try {
            val result = useCase.get()
            state.update { it.copy(entity = result, isLoading = false) }
        } catch (e: Exception) {
            state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
        }
    }
}
```

**`…/presentation/external/view/<Feature>Screen.kt`**
```kotlin
package <pkg>.kit.<feature>.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import <pkg>.kit.<feature>.domain.useCases.<Feature>UseCase
import <pkg>.kit.<feature>.presentation.external.view.models.<Feature>ViewModel
import <pkg>.kit.<feature>.presentation.internal.view.<Feature>View
import org.kodein.di.compose.localDI
import org.kodein.di.instance

@Composable
fun <Feature>Screen() {
    val di = localDI()
    val useCase: <Feature>UseCase by di.instance()
    val viewModel: <Feature>ViewModel = viewModel { <Feature>ViewModel(useCase) }

    LaunchedEffect(Unit) { viewModel.load() }

    <Feature>View(viewModel)
}
```

**`…/presentation/internal/view/<Feature>View.kt`**
```kotlin
package <pkg>.kit.<feature>.presentation.internal.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import <pkg>.kit.<feature>.presentation.external.view.models.<Feature>ViewModel

@Composable
internal fun <Feature>View(viewModel: <Feature>ViewModel) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> Text("Loading…")
        state.error != null -> Text("Error: ${state.error}")
        state.entity != null -> Text("<Feature>: ${state.entity!!.id}")
    }
}
```

**`…/presentation/src/commonTest/kotlin/<pkg>/kit/<feature>/presentation/external/view/models/<Feature>ViewModelTest.kt`**
```kotlin
package <pkg>.kit.<feature>.presentation.external.view.models

import <pkg>.kit.<feature>.domain.entities.<Feature>Entity
import <pkg>.kit.<feature>.domain.useCases.<Feature>UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class <Feature>ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialStateIsEmpty() = runTest {
        val vm = <Feature>ViewModel(SuccessUseCase())
        assertNull(vm.state.value.entity)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun loadSetsEntityOnSuccess() = runTest {
        val vm = <Feature>ViewModel(SuccessUseCase())
        vm.load()
        advanceUntilIdle()
        assertNotNull(vm.state.value.entity)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun loadSetsErrorOnFailure() = runTest {
        val vm = <Feature>ViewModel(FailureUseCase())
        vm.load()
        advanceUntilIdle()
        assertNull(vm.state.value.entity)
        assertEquals("stub error", vm.state.value.error)
    }

    private class SuccessUseCase : <Feature>UseCase {
        override suspend fun get() = <Feature>Entity(id = "test-id")
    }

    private class FailureUseCase : <Feature>UseCase {
        override suspend fun get(): <Feature>Entity = throw Exception("stub error")
    }
}
```

### Step 2 — Register in `settings.gradle.kts`

Add three entries inside the existing `listOf(...)` block:

```kotlin
Module(":client-kit:<feature>-data",         "client-kit/<feature>/data"),
Module(":client-kit:<feature>-domain",       "client-kit/<feature>/domain"),
Module(":client-kit:<feature>-presentation", "client-kit/<feature>/presentation"),
```

### Step 3 — Export in `shared/build.gradle.kts`

Add three entries to the `modules` list:

```kotlin
projects.clientKit.<feature>Data,
projects.clientKit.<feature>Domain,
projects.clientKit.<feature>Presentation,
```

### Step 4 — Wire DI in `shared/src/commonMain/…/AppDI.kt`

```kotlin
import <pkg>.kit.<feature>.presentation.external.<feature>Module

val appDI = DI {
    // existing imports …
    import(<feature>Module)
}
```

## Adding a Multi-step Flow

When a feature requires multiple screens sharing ephemeral state (auth pattern: phone → OTP), use a Context object to bridge ViewModels without navigation args.

**Step 1 — Create context in `<feature>/domain/contexts/`:**
```kotlin
class <Feature>FlowContext {
    var inputData: String = ""
    lateinit var serverSessionId: String
}
```

**Step 2 — Bind as singleton in DI module:**
```kotlin
bind<<Feature>FlowContext>() with singleton { <Feature>FlowContext() }
```

**Step 3 — Step-1 ViewModel writes to context, then navigates:**
```kotlin
class Step1ViewModel(
    private val useCase: AuthUseCase,
    private val context: <Feature>FlowContext,
    private val nav: NavigationContext,
) : ViewModel() {
    fun onSubmit() = viewModelScope.launch {
        val sessionId = useCase.initiate(state.value.input)
        context.inputData = state.value.input
        context.serverSessionId = sessionId
        nav.navigate(<Feature>Destination.Step2)
    }
}
```

**Step 4 — Step-2 ViewModel reads from context:**
```kotlin
class Step2ViewModel(
    private val useCase: AuthUseCase,
    private val context: <Feature>FlowContext,
) : ViewModel() {
    fun onSubmit() = viewModelScope.launch {
        useCase.complete(state.value.code, context.serverSessionId)
    }
}
```

See `client-kit/auth/` for the complete working example (`OtpAuthorizationContext`, `InitiateViewModel`, `OtpViewModel`).

## Gradle Convention Plugins

Located in `build-logic/`. Each module declares exactly one plugin alias from `libs.versions.toml`.

| Plugin alias | Used by | Provides |
|---|---|---|
| `setup.multiplatform.feature` | `domain`, `data` layers | KMP targets, Kodein DI, Serialization, Kermit, coroutines-test |
| `setup.multiplatform.feature.compose` | `presentation` layers, `shared` | everything above + Compose Multiplatform, Material3, lifecycle, `kodein-di-compose` |
| `setup.multiplatform.infrastructure` | `infrastructure/api/*` | KMP targets, Ktor, multiplatform-foundation, Serialization |

`AndroidConfig.kt` centralizes SDK versions and JVM target — change versions there, not per-module.

Version catalog at `gradle/libs.versions.toml` is the single source of truth for all dependency versions.

## Adapting the Template to a New Project

After forking, replace all project-specific identifiers. The table below lists every token that needs changing and where to find it.

### Identifier Map

| Token | Current value | Where it appears |
|-------|--------------|-----------------|
| Root project name | `ugg-client` | `settings.gradle.kts:1` |
| Gradle group | `com.ugg` | `build.gradle.kts:8` |
| Android applicationId | `com.ugg.client.android` | `androidApp/build.gradle.kts:9` |
| Android namespace (app) | `com.ugg.client.android` | `androidApp/build.gradle.kts:6` |
| iOS framework name | `UggShared` | `shared/build.gradle.kts:14` |
| Kotlin root package | `com.ugg.client` | All `*.kt` files and module `namespace` fields |
| External API group | `com.ugg` | `gradle/libs.versions.toml:62` |

### Step-by-Step Rename

**1. `settings.gradle.kts`** — project name used by Gradle and IDE:
```kotlin
rootProject.name = "your-project-name"   // was: "ugg-client"
```

**2. `build.gradle.kts` (root)** — Maven group for published artifacts:
```kotlin
group = "com.yourcompany"                 // was: "com.ugg"
```

**3. `androidApp/build.gradle.kts`** — Android application identity:
```kotlin
namespace     = "com.yourcompany.yourapp.android"
applicationId = "com.yourcompany.yourapp.android"
```

**4. `shared/build.gradle.kts`** — iOS framework name (used in Xcode project):
```kotlin
namespace = "com.yourcompany.yourapp.shared"
val frameworkName = "YourAppShared"       // was: "UggShared"
```
Update the Xcode project to reference the renamed `.xcframework`.

**5. All module `build.gradle.kts` files** — Android namespaces (9 files under `client-kit/`):
```
com.ugg.client.kit.core.data         → com.yourcompany.yourapp.kit.core.data
com.ugg.client.kit.core.domain       → com.yourcompany.yourapp.kit.core.domain
com.ugg.client.kit.core.presentation → com.yourcompany.yourapp.kit.core.presentation
... same pattern for auth and navigation modules
```

**6. Kotlin source files** — package declarations and imports in all `*.kt` files:

Use IDE rename (Right-click package → Refactor → Rename) or a global find-and-replace:
```
com.ugg.client  →  com.yourcompany.yourapp
```
This covers every `package` declaration and `import` statement across all modules.

**7. `gradle/libs.versions.toml`** — external API dependency:
```toml
ugg-client-api-core = { group = "com.yourcompany", name = "your-api-core", ... }
```

**8. Class and file name prefixes** — the template uses `Template`/`template-` prefixes in a few places:

| Current name | Where | Rename to |
|---|---|---|
| `UggShared` (framework) | `shared/build.gradle.kts` | `YourAppShared` |
| `AppDI` | `shared/.../AppDI.kt` | Keep or rename to `YourAppDI` |
| `ContentView` | `shared/.../ContentView.kt` | Keep or rename |

Classes inside `client-kit` (e.g. `AuthViewModel`, `NavigationContext`) carry no project-specific prefix — they can stay as-is.

### Checklist

```
[ ] settings.gradle.kts — rootProject.name
[ ] build.gradle.kts (root) — group
[ ] androidApp/build.gradle.kts — namespace, applicationId
[ ] shared/build.gradle.kts — namespace, frameworkName
[ ] client-kit/**/build.gradle.kts — namespace (9 files)
[ ] All *.kt files — package declarations and imports
[ ] gradle/libs.versions.toml — API dependency group/name
[ ] Xcode project — xcframework reference and bundle ID
[ ] CLAUDE.md — update Project Overview section
```

---

## Current State

Authentication uses a 2-step phone → OTP flow. In `AppEnvironment.MOCK` mode (default), `StubAuthUseCase` accepts any phone number and OTP `123456`. In `AppEnvironment.PRODUCTION` mode, `SharedAuthUseCase` delegates to `AuthService` — replace the stub bodies in `client-kit/auth/data/src/.../services/AuthService.kt` with real Ktor calls when the backend is ready. No other changes needed — the DI binding in `AuthModule` switches implementations automatically based on `AppEnvironment`.
