---
name: add-feature-module
description: Use when the user asks to add, create, or scaffold a new feature module with domain, data, and presentation layers in this KMM project.
---

# Add Feature Module

Scaffold a Clean Architecture feature (domain + data + presentation). The layer rules themselves
live in `clean-domain-layer`, `clean-data-layer` and `clean-presentation-layer` — load whichever
you are writing. This skill is only the file list and the wiring.

**All identifiers are derived from existing project files, never hardcoded.**

## Step 0 — Discover project context

| Value | Where to read | How to extract |
|-------|--------------|----------------|
| `<pkg>` (root package) | `androidApp/build.gradle.kts` | `namespace = "X.android"` → drop `.android` |
| `<appDiFile>` | `shared/src/commonMain/**/*.kt` | file containing `DI {` and `import(…Module)` — currently `TemplateApp.kt` |
| module list | `settings.gradle.kts` | the `listOf(Module(…))` block |
| export list | `shared/build.gradle.kts` | `val modules = listOf(…)` |

Derive once: `<feature>` (lowercase, `profile`), `<Feature>` (PascalCase, `Profile`),
`<featureCamel>` (Gradle accessor, `profileData` / `profileDomain` / `profilePresentation`).

## Step 1 — Create 14 files

### domain

**`domain/build.gradle.kts`** — `alias(libs.plugins.setup.multiplatform.feature)`, namespace
`<pkg>.kit.<feature>.domain`, no dependencies.

**`domain/entities/<Feature>.kt`** — the entity is the shape the View needs, not the wire format:
```kotlin
package <pkg>.kit.<feature>.domain.entities

data class <Feature>(
    val id: String = "",
)
```

**`domain/useCases/<Feature>UseCase.kt`** — interface only. No `Result`; failures are exceptions:
```kotlin
package <pkg>.kit.<feature>.domain.useCases

interface <Feature>UseCase {
    suspend fun get(): <Feature>
}
```

There is **no `states/` and no `contexts/`** in domain. States belong to presentation; cross-screen
updates are a `Flow` on the UseCase.

### data

**`data/build.gradle.kts`** — `setup.multiplatform.feature`, namespace `<pkg>.kit.<feature>.data`:
```kotlin
commonMain.dependencies {
    implementation(projects.clientKit.<featureCamel>Domain)
    implementation(projects.clientKit.coreDomain)
}
```

**`data/repositories/Shared<Feature>Repository.kt`** — the real implementation:
```kotlin
package <pkg>.kit.<feature>.data.repositories

class Shared<Feature>Repository : <Feature>UseCase {
    override suspend fun get(): <Feature> = TODO("call the service, map the DTO")
}
```

**`data/repositories/Mock<Feature>Repository.kt`** — the `AppEnvironment.MOCK` fake:
```kotlin
package <pkg>.kit.<feature>.data.repositories

class Mock<Feature>Repository : <Feature>UseCase {
    override suspend fun get(): <Feature> = <Feature>(id = "mock")
}
```

**`data/DI+<feature>DataModule.kt`** — binds the repository, nothing else:
```kotlin
package <pkg>.kit.<feature>.data

val <feature>DataModule get() = DI.Module("<feature>-data") {
    bind<<Feature>UseCase>() with factory {
        when (instance<AppEnvironment>()) {
            AppEnvironment.PRODUCTION -> Shared<Feature>Repository()
            AppEnvironment.MOCK -> Mock<Feature>Repository()
        }
    }
}
```

If both branches would build the same thing, drop the `when` and bind directly — see
`splashDataModule`.

### presentation

**`presentation/build.gradle.kts`** — `setup.multiplatform.feature.compose`, namespace
`<pkg>.kit.<feature>.presentation`:
```kotlin
commonMain.dependencies {
    implementation(projects.clientKit.<featureCamel>Data)
    implementation(projects.clientKit.<featureCamel>Domain)
    implementation(projects.clientKit.coreDomain)
}
```

**`presentation/external/states/<Feature>State.kt`** — all fields defaulted, `error: Throwable?`:
```kotlin
data class <Feature>State(
    val entity: <Feature>? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
```

**`presentation/external/<Feature>Module.kt`** — imports the data module, binds only ViewModels:
```kotlin
val <feature>Module: DI.Module
    get() = DI.Module("<feature>.module") {
        import(<feature>DataModule)

        bind<<Feature>ViewModel>() with factory { <Feature>ViewModel(instance()) }
    }
```

**`presentation/external/view/models/<Feature>ViewModel.kt`** — one state, one handler:
```kotlin
class <Feature>ViewModel(private val useCase: <Feature>UseCase) : ViewModel() {
    val state: StateFlow<<Feature>State>
        field = MutableStateFlow(<Feature>State())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@<Feature>ViewModel::class.toString() }

        state.update { it.copy(error = throwable, isLoading = false) }
    }

    init {
        fetch()
    }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            state.update { it.copy(entity = useCase.get()) }
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }
}

@Suppress("unused")
fun DI.<feature>ViewModel(): <Feature>ViewModel = this.direct.instance()
```

**`presentation/external/view/<Feature>Screen.kt`** — resolves the ViewModel, collects state:
```kotlin
@Composable
fun <Feature>Screen() {
    val viewModel: <Feature>ViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    <Feature>View(
        entity = state.entity,
        error = state.error,
        isLoading = state.isLoading,
        onRetry = { viewModel.fetch() },
    )
}
```

**`presentation/internal/view/<Feature>View.kt`** — values and lambdas, never a ViewModel:
```kotlin
@Composable
internal fun <Feature>View(
    entity: <Feature>?,
    error: Throwable?,
    isLoading: Boolean,
    onRetry: () -> Unit,
) {
    when {
        isLoading -> CircularProgressIndicator()
        error != null -> Button(onClick = onRetry) { Text(error.message ?: "Unknown error") }
        entity != null -> Text("<Feature>: ${entity.id}")
    }
}
```

**`presentation/src/commonTest/…/<Feature>ViewModelTest.kt`** — fakes, no mocks:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class <Feature>ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun fetchSetsEntityOnSuccess() = runTest {
        val vm = <Feature>ViewModel(SuccessUseCase())
        advanceUntilIdle()

        assertEquals("test-id", vm.state.value.entity?.id)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun fetchSetsErrorOnFailure() = runTest {
        val vm = <Feature>ViewModel(FailureUseCase())
        advanceUntilIdle()

        assertNull(vm.state.value.entity)
        assertEquals("stub error", vm.state.value.error?.message)
        assertFalse(vm.state.value.isLoading)
    }

    private class SuccessUseCase : <Feature>UseCase {
        override suspend fun get() = <Feature>(id = "test-id")
    }

    private class FailureUseCase : <Feature>UseCase {
        override suspend fun get(): <Feature> = throw Exception("stub error")
    }
}
```

`init { fetch() }` means the test only needs `advanceUntilIdle()` — no explicit `load()` call.

## Step 2 — Register in `settings.gradle.kts`

```kotlin
Module(":client-kit:<feature>-data",         "client-kit/<feature>/data"),
Module(":client-kit:<feature>-domain",       "client-kit/<feature>/domain"),
Module(":client-kit:<feature>-presentation", "client-kit/<feature>/presentation"),
```

Read the file first — match the existing prefix and spacing.

## Step 3 — Export in `shared/build.gradle.kts`

```kotlin
projects.clientKit.<featureCamel>Data,
projects.clientKit.<featureCamel>Domain,
projects.clientKit.<featureCamel>Presentation,
```

Missing here means iOS never sees the module.

## Step 4 — Wire DI in `<appDiFile>`

```kotlin
import <pkg>.kit.<feature>.presentation.external.<feature>Module

// inside the DI { } block, after import(coreDataModule):
import(<feature>Module)
```

Only the presentation module is imported here — it pulls in the data module itself.

## Step 5 — When the feature talks to a backend

The repository must not call Ktor directly. Add a service and bind it once:

1. Create `infrastructure/api/<feature>` with `setup-multiplatform-infrastructure`, add
   `api(projects.infrastructure.apiAuth)`, and write
   `<pkg>.infrastructure.api.<feature>.services.<Feature>Service(auth: TemplateAuth)`.
2. Bind it in `client-kit/core/data`'s `coreDataModule` — the single place every service is
   declared:
   ```kotlin
   bind<<Feature>Service>() with scoped(TemplateCoreScope).singleton { <Feature>Service(instance()) }
   ```
3. Resolve it in `<feature>DataModule` through the core context — a plain `instance()` will not
   find a scoped binding:
   ```kotlin
   bind<<Feature>UseCase>() with factory {
       val core: TemplateCore = instance()

       when (instance<AppEnvironment>()) {
           AppEnvironment.PRODUCTION -> Shared<Feature>Repository(on(core).instance())
           AppEnvironment.MOCK -> Mock<Feature>Repository()
       }
   }
   ```
4. Add `implementation(projects.infrastructure.api<Feature>)` and
   `implementation(projects.clientKit.coreData)` to the feature's `data/build.gradle.kts`.

Screens that must re-read after another screen writes get a `Flow` from the service — see
`clean-data-layer`, section *Observable data*.

## Step 6 — Verify

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :client-kit:<feature>-presentation:iosSimulatorArm64Test
```

`testAndroidHostTest` is expected to fail in modules constructing `MultiplatformCore` (JVM class
version mismatch, see CLAUDE.md) — use the iOS target.

## Common mistakes

| Mistake | Fix |
|---------|-----|
| Hardcoding `com.template.client` | Read `androidApp/build.gradle.kts` namespace first |
| `setup.multiplatform.library` on a feature module | domain/data use `feature`, presentation uses `feature.compose` |
| States in `domain/states/` | They are UI shapes — `presentation/external/states/` |
| Repository named `Shared<Feature>UseCase` | The interface is the UseCase; the implementation is a Repository |
| `instance()` for a scoped service | `on(core).instance()`, with `TemplateCore` resolved first |
| Binding the repository in the presentation module | It belongs in `<feature>DataModule` |
| `try/catch` in the ViewModel instead of the handler | One `CoroutineExceptionHandler`, `try/finally` for flags |
| Passing the ViewModel into the View | The Screen collects state; the View takes values and lambdas |
