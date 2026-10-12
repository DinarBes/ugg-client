---
name: clean-presentation-layer
description: Use when writing a UI state class, a ViewModel, a DI module, or a Compose Screen/View in a `presentation` module. Triggers on "добавь ViewModel", "экран", "presentation слой", DI.Module wiring, or any file under client-kit/<feature>/presentation.
---

# Presentation Layer

Four things live here: UI states (`external/states/`), the DI graph (`external/<Feature>Module.kt`),
ViewModels (`external/view/models/`), and Compose views (`external/view/` + `internal/view/`).

## States

`states/` holds the UI state data classes, one per screen, all fields defaulted. They live here and
not in `domain`: a state is the shape of *this screen*, not a rule of the feature — it carries
`isRefreshing` and `isFormExpanded`, which no UseCase knows or should know about.

```kotlin
package <pkg>.kit.<feature>.presentation.external.states

data class DiabetesRiskState(
    val risk: DiabetesRisk = DiabetesRisk(),
    val inputs: DiabetesRiskInputs = DiabetesRiskInputs(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)
```

A state is composed of `domain` entities plus UI-only flags. It never re-declares what an entity
already models, and it never adds wire-shaped fields — if the View needs a formatted value, the
entity should already carry it (see `clean-domain-layer`).

Keep `error: Throwable?`, not `String?` — the View decides how to render it, and the ViewModel's
`CoroutineExceptionHandler` already has the throwable in hand.

## ViewModel

```kotlin
package <pkg>.kit.<feature>.presentation.external.view.models

@Suppress("unused")
fun DI.diabetesRiskViewModel(): DiabetesRiskViewModel = direct.instance()

class DiabetesRiskViewModel(
    private val useCase: DiabetesRiskUseCase,
) : ViewModel() {
    val state: StateFlow<DiabetesRiskState>
        field = MutableStateFlow(DiabetesRiskState())

    private val unexpectedExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable) { this@DiabetesRiskViewModel::class.toString() }

        state.update { it.copy(error = throwable) }
    }

    init {
        observe()
    }

    fun setFormExpanded(expanded: Boolean) = state.update { it.copy(isFormExpanded = expanded) }
    fun updateInputs(inputs: DiabetesRiskInputs) = state.update { it.copy(inputs = inputs) }

    fun fetch() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { DiabetesRiskState() }

        fetchContent()
    }

    fun refresh() = viewModelScope.launch(unexpectedExceptionHandler) {
        state.update { it.copy(isRefreshing = true) }

        try {
            fetchContent()
        } finally {
            state.update { it.copy(isRefreshing = false) }
        }
    }

    fun save() = viewModelScope.launch(unexpectedExceptionHandler) {
        val inputs = state.value.inputs

        state.update { it.copy(isLoading = true, error = null) }

        try {
            // Сохранение дёргает refresh внутри сервиса, поэтому подписчики observeRisk()
            // перечитают сами — сообщать другим экранам ничего не нужно.
            useCase.save(inputs)
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }

    private fun observe() = viewModelScope.launch(unexpectedExceptionHandler) {
        useCase.observeRisk().collect(::show)
    }

    private fun show(risk: DiabetesRisk) = state.update { it.copy(risk = risk, inputs = risk.inputs) }
}
```

Non-negotiables:

- **`val state: StateFlow<X>` + `field = MutableStateFlow(X())`** — Kotlin 2.3 backing field. Never
  expose the mutable flow, never keep a second `_state` property.
- **One `CoroutineExceptionHandler` per ViewModel**, installed on every `viewModelScope.launch`.
  It logs and parks the throwable in state. Consequence: no `try/catch (e: Exception)` around the
  happy path — catch only a feature's own exception from `domain/exceptions/`, and only when the
  screen reacts to it differently (a field-level error, a retry prompt):

  ```kotlin
  try {
      show(useCase.save(inputs))
  } catch (e: DiabetesRiskInputsInvalidException) {
      state.update { it.copy(invalidField = e.field) }
  }
  ```

  Everything else reaches the handler and lands in `state.error`. No `Result`/`getOrThrow()` —
  UseCases throw (see `clean-domain-layer`).
- **`try/finally` for flags**, not `try/catch/finally` — the flag must clear even when the handler
  takes over.
- Synchronous UI intents are one-liners: `fun setX(v: T) = state.update { it.copy(x = v) }`.
- `init { fetch() }` for the initial load — not `LaunchedEffect` in the Screen, unless the load
  depends on a Compose-side argument.
- A private `show(entity)` collapses "state got new data" into one place.
- `fun DI.<feature>ViewModel(): X = direct.instance()` next to the class — this is the Swift-callable
  DI helper (SKIE exports functions, not extension properties). Kotlin-only code can use
  `DI.resolve<X>()` from `core/domain`.

## DI module

The presentation module binds **only ViewModels**. Repositories come from the feature's data module,
which it imports; services come from `core/data` (see `clean-data-layer`):

```kotlin
val <feature>Module: DI.Module
    get() = DI.Module("<feature>.module") {
        import(<feature>DataModule)

        bind<<Feature>ViewModel>() with factory { <Feature>ViewModel(instance(), instance()) }
    }
```

`factory` for ViewModels. Register the feature module in `TemplateApp` / `AppDI.kt` — `coreDataModule`
is imported there first, so the scoped services already exist by the time a feature resolves one.

A ViewModel never resolves a service directly. It takes a UseCase, the UseCase implementation takes
the service — that is the only chain.

## Screen vs View

- `external/view/<Feature>Screen.kt` — **public**, resolves the ViewModel, collects state, passes
  navigation arguments down.
- `internal/view/<Feature>View.kt` — **`internal`**, takes plain values and lambdas. No ViewModel,
  no DI, no `collectAsState`.

```kotlin
@Composable
fun OtpScreen(
    phone: String,
    sessionId: String,          // навигационный аргумент, не общее состояние
) {
    val viewModel: OtpViewModel by rememberViewModel()
    val state by viewModel.state.collectAsState()

    OtpView(
        otp = state.otp,
        error = state.error,
        isLoading = state.isLoading,
        onOtpChanged = { viewModel.onStateChange(state.copy(otp = it)) },
        onSubmit = { viewModel.onSubmit(sessionId) },
    )
}
```

The View gets values, not a ViewModel: it stays previewable, and the Screen is the only place that
knows DI exists. `rememberViewModel()` from `kodein-di-framework-compose` resolves through the
bound `factory`.

## Checklist

- [ ] state class in `external/states/`, all fields defaulted, `error: Throwable?`
- [ ] `state` exposed as `StateFlow` with `field =` backing
- [ ] every `launch` carries `unexpectedExceptionHandler`
- [ ] loading/refreshing flags cleared in `finally`
- [ ] DI helper function next to the ViewModel; module registered in `AppDI.kt`
- [ ] presentation module binds only ViewModels and imports `<feature>DataModule`
- [ ] `internal` View takes values and lambdas, never a ViewModel; Screen collects the state
- [ ] ViewModel test in `commonTest` with fake UseCases (see `InitiateViewModelTest`)
