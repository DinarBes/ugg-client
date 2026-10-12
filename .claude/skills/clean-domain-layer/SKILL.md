---
name: clean-domain-layer
description: Use when writing or reviewing code in a `domain` module — entities, UseCase interfaces, domain exceptions. Triggers on "добавь UseCase", "сущность", "domain слой", or any new interface/entity under client-kit/<feature>/domain.
---

# Domain Layer

`domain` has no framework dependencies and no knowledge of transport. It contains:
`entities/`, `useCases/`, `exceptions/`.

## Entities are not DTOs

A DTO lives in `infrastructure` and mirrors the wire format. An **entity dictates the shape the
View needs** — already formatted, already resolved.
The DTO → entity mapping happens in `data` (see `clean-data-layer`), never here.

Rules of thumb:
- If a field exists only because the backend sends it, it does not belong in the entity.
- If the View would have to compute/format something from it, precompute it in the entity instead
  (`levelName`, `blocks`, …).
- Entities carry no annotations from `kotlinx.serialization` — that is a DTO smell.
- Nullable fields are fine: `null` is a legitimate value when the data may genuinely be absent.

```kotlin
package <pkg>.kit.<feature>.domain.entities

data class DiabetesRisk(
    val score: Double = 0.0,
    val levelName: String = "",       // already localized — View just renders it
    val comment: String? = null,      // nullable is fine — there may be no comment
    val blocks: List<RiskBlock> = emptyList(),
    val inputs: DiabetesRiskInputs = DiabetesRiskInputs(),
)
```

## UseCase

A UseCase is an **interface** that names the feature's capabilities and fixes input/output formats.
Only entities (and `Flow` of them) cross this boundary.

```kotlin
package <pkg>.kit.<feature>.domain.useCases

interface DiabetesRiskUseCase {
    fun notifications(): Flow<DiabetesRiskNotification>

    suspend fun fetch(): DiabetesRisk

    /** Сохраняет введённые данные и возвращает пересчитанный риск. */
    suspend fun save(inputs: DiabetesRiskInputs): DiabetesRisk
}
```

The interface keeps the `UseCase` name; only its implementation in `data` is a repository
(see `clean-data-layer`).

- One-shot request → `suspend fun` returning an entity.
- Stream of events → `fun` returning `Flow<Entity>` (not suspend — subscribing is cheap).
- Failure → throw a domain exception. **Never `Result<Entity>`**: it carries no meaning the
  signature doesn't already have, and forces every caller through `getOrThrow()` anyway.
- KDoc only where the signature is not self-evident — e.g. when a method both writes and returns.

## Exceptions

Each feature declares its own exceptions in `exceptions/`, one file per exception. They are part of
the domain vocabulary — the way a failure is *named*, so the caller can branch on the name instead
of parsing a message or an HTTP code.

```kotlin
package <pkg>.kit.<feature>.domain.exceptions

class DiabetesRiskUnavailableException : Exception("Расчёт риска временно недоступен")

class DiabetesRiskInputsInvalidException(val field: DiabetesRiskField) : Exception()
```

- Name the *domain* condition, not the transport: `DiabetesRiskUnavailableException`, not
  `Http503Exception`. No `HttpStatusCode`, no Ktor types in the constructor.
- Declare one only when someone branches on it. A failure the UI merely displays needs no class —
  let it propagate to the ViewModel's `CoroutineExceptionHandler`.
- Carry data the caller needs to react (`field` above); otherwise leave it empty.

`data` maps service/transport exceptions onto these (see `clean-data-layer`), so `domain` never
learns what a 401 is.

## Cross-screen refresh is a Flow, not a context

When screen B changes data on the server and screen A must re-read it, express that as a `Flow` on
the UseCase. **No `contexts/` package, no shared mutable object between ViewModels.**

```kotlin
interface ProfileUseCase {
    fun observeProfile(): Flow<Profile>

    suspend fun update(name: String)
}
```

Screen A collects `observeProfile()`; screen B calls `update()`. B never tells A anything — the
service re-emits and A re-renders. The trigger lives in the `infrastructure/api` service, which
is bound once as a scoped singleton in `core/data` — that single instance is what makes both
screens see the same signal (see `clean-data-layer`). Neither screen knows the other exists.

Why not a context object: it is shared mutable state that two ViewModels write to, ordering between
them is implicit, and nothing makes A actually re-read. A `Flow` makes the dependency one-directional
and typed.

Passing a value *forward* through a flow (phone → OTP `sessionId`) is a different problem: that is a
navigation argument, not shared state. Put it on the destination — `AuthFlow.Otp(phone, sessionId)`.

## Checklist

- [ ] no imports from `infrastructure`, Ktor, Compose, or `androidx`
- [ ] entity has no `@Serializable`, no wire-only fields
- [ ] UseCase is an interface; the implementation is `<Feature>Repository` in `data`
- [ ] no `Result<T>` in UseCase signatures — failures are domain exceptions
- [ ] exceptions name a domain condition, not an HTTP code
- [ ] cross-screen updates are `Flow` on a UseCase; no `contexts/` package
- [ ] data passed between steps of one flow goes in the navigation destination
- [ ] no UI state classes here — `states/` lives in `presentation`
