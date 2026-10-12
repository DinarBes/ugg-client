---
name: clean-data-layer
description: Use when implementing a UseCase or mapping DTOs to entities in a `data` module. Triggers on "реализуй UseCase", "маппер", "data слой", "репозиторий", Shared*/Mock*Repository, or any file under client-kit/<feature>/data.
---

# Data Layer

`data` depends on `domain` plus whatever sources of truth the feature actually has — an HTTP
service, the keystore, a local database, a third-party SDK (аналитика, карты, платежи, push),
the platform itself (геолокация, камера, биометрия), an in-memory cache. There is no fixed list,
and one feature may combine several.

Whatever the source, `data` treats it the same way: it contains exactly two kinds of thing,
`mappers/` and `repositories/`. Nothing else, and no Compose.

**The source's own types never leave `data`.** `HttpResponse`, a Room entity, an SDK's
`FirebaseUser`, a `CLLocation` — all of them stop here and continue as `domain` entities. That is
the one rule that makes the source swappable.

**Wrap each source behind a service, and keep the service out of the feature's `data` module.**
HTTP services live in `infrastructure/api/<name>/services/`; an SDK or platform capability gets
the same treatment in its own `infrastructure/<name>` module, so the SDK's dependency does not
leak into every feature that touches it. Every service is bound in exactly one place —
`client-kit/core/data` — see **Services and DI** below.

## Mappers

DTO → entity conversion lives in its own package so UseCase bodies stay readable.

**File name: `<EntityName>+Dto.kt`** — e.g. `DiabetesRisk+Dto.kt`, in `data/mappers/`.
Write them as extension functions named `to<Entity>()`:

```kotlin
package <pkg>.kit.<feature>.data.mappers

fun HealthRiskLevelDto.toDiabetesRisk() = DiabetesRisk(
    score = score,
    level = level,
    levelName = HealthRiskCopy.levelName(level),
    blocks = diabetesRiskBlocks(level),
    inputs = inputs,
)
```

- One file per entity, all directions for that entity in it (`toEntity()`, `toRequest()`).
- Mappers are pure: no suspend, no network, no DI. If a mapper needs a service, it is not a mapper.
- Defaults and formatting belong here — this is where the wire format stops existing.

## Repositories — the UseCase implementations

A `domain` UseCase interface is implemented in `data` by a **repository**: drop the `UseCase`
suffix, add `Repository`. `ProfileUseCase` → `ProfileRepository`, in `repositories/`.

Keep the environment prefix: `Shared<Feature>Repository` (real) and `Mock<Feature>Repository`
(fake for `AppEnvironment.MOCK`). Both implement the `domain` interface, dependencies come in
through the constructor.

| domain | data |
|---|---|
| `useCases/ProfileUseCase.kt` (interface) | `repositories/SharedProfileRepository.kt` |
| | `repositories/MockProfileRepository.kt` |

```kotlin
class SharedDiabetesRiskRepository(
    private val service: MeasurementsService,
) : DiabetesRiskUseCase {

    // GET возвращает только балл: форма приедет пустой, прошлые значения бэкенд не отдаёт.
    override suspend fun fetch(): DiabetesRisk =
        scoreOrNull { service.getDiabetesRiskScore() }.toRisk(DiabetesRiskInputs())

    override suspend fun save(inputs: DiabetesRiskInputs): DiabetesRisk {
        val request = DiabetesRiskInputRequest(waistCircumference = inputs.waistCircumference)

        if (request.isEmpty) return fetch().copy(inputs = inputs)

        return scoreOrNull { service.submitDiabetesRiskInputs(request) }.toRisk(inputs)
    }

    private fun Int?.toRisk(inputs: DiabetesRiskInputs): DiabetesRisk {
        val score = this?.toDouble() ?: return DiabetesRisk(inputs = inputs)

        return levelOf(score, DiabetesRisk.DEFAULT_BOUNDS).toDiabetesRisk()
    }
}
```

Shape of a method body: build the request → call the service → map → return. Three or four lines.
Anything longer is either a private helper in the same class (like `toRisk` above) or a mapper.

**Comments explain the *why*, and only where the code alone lies about it** — a backend quirk, a
deliberately skipped round-trip. Not a restatement of the next line.

## Observable data originates in `infrastructure/api`

**Every `Flow` of server data starts in the service, never in `data` and never in a ViewModel.**
The chain is one-directional, and each link only changes the type:

```
infrastructure/api   ProfileService.observeProfile() : Flow<ProfileResponse>   // источник
        ↓                                                                      // DTO
client-kit/data      SharedProfileRepository.observeProfile() : Flow<Profile>     // .map { it.toProfile() }
        ↓                                                                      // сущность
client-kit/presentation  ViewModel.observe() -> state                          // .collect(::show)
```

A screen that must re-read after another screen writes gets this `Flow`, not a context object.
The service re-emits and every subscriber re-renders; nobody notifies anybody.

**The service holds a signal, not the data.** Keep the server the single source of truth — a
`MutableSharedFlow<Unit>` that says "re-read", and the data is fetched on demand:

```kotlin
class ProfileService(private val auth: TemplateAuth) {
    private val refreshProfile = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun observeProfile(): Flow<ProfileResponse> =
        refreshProfile
            .onStart { emit(Unit) }      // первая загрузка без внешнего триггера
            .map { getProfile() }

    suspend fun updateProfile(name: String) {
        api.updateProfile(name)

        refreshProfile.emit(Unit)        // подписчики перечитают
    }
}
```

Do **not** cache the response in a `MutableStateFlow` inside the service. That turns a transport
wrapper into a cache: lifetime, invalidation, what happens on logout, and staleness all become its
problems. Caching is a deliberate decision with its own reasons — not the default shape.

Two notes on the code above, both about things that silently do nothing if omitted:
`onStart` is what makes the first load happen without an external trigger, and a bare
`MutableSharedFlow()` has no buffer, so its `tryEmit` always returns `false` and drops the value
(`AuthContext` in this repo was written that way and login events reached nobody).

Swap `map` for `flatMapLatest` if a refresh arriving mid-request should cancel the stale one; add
`shareIn` if one request per subscriber starts to show. Neither is needed to start.

The UseCase maps it to entities and nothing else:

```kotlin
override fun observeProfile(): Flow<Profile> = service.observeProfile().map { it.toProfile() }
```

Each collector runs its own request. If several screens observe at once and that shows up, share it:
`.shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)` — the scope comes from the
service's constructor, never from `GlobalScope`.

## Services and DI

Every service is a **scoped singleton bound in `client-kit/core/data`**. One place for all of them,
one instance each — which is what lets two screens share the same refresh signal. The pieces:

**1. The scope** — `client-kit/core/data/scopes/TemplateCoreScope.kt`. Ties service lifetime to the
`TemplateCore` instance:

```kotlin
object TemplateCoreScope : Scope<TemplateCore> {
    private val registries = mutableMapOf<TemplateCore, ScopeRegistry>()

    override fun getRegistry(context: TemplateCore): ScopeRegistry =
        registries.getOrPut(context) { StandardScopeRegistry() }

    fun close(core: TemplateCore) = registries.remove(core)?.clear()
}
```

**2. The one service registry** — `client-kit/core/data/DI+coreDataModule.kt`. Every service in the
app is declared here and nowhere else, so a duplicate declaration is visible instead of being a
runtime `OverridingException`:

```kotlin
val coreDataModule get() = DI.Module("core-data") {
    bind<AuthService>() with scoped(TemplateCoreScope).singleton { AuthService(instance()) }
    bind<ProfileService>() with scoped(TemplateCoreScope).singleton { ProfileService(instance()) }
}
```

**3. The feature's data module** — `client-kit/<feature>/data/DI+<feature>DataModule.kt`. Binds the
repository, pulling scoped services through the core context:

```kotlin
val authDataModule get() = DI.Module("auth-data") {
    bind<AuthUseCase>() with factory {
        val core: TemplateCore = instance()

        when (instance<AppEnvironment>()) {
            AppEnvironment.PRODUCTION -> SharedAuthRepository(on(core).instance())
            AppEnvironment.MOCK -> MockAuthRepository()
        }
    }
}
```

`on(core).instance()` is mandatory for a scoped binding: a plain `instance()` resolves with no
context and will not find it. Resolve `TemplateCore` once at the top of the block and reuse it.

**4. Registration** — `TemplateApp` imports `coreDataModule` **before** the feature modules; each
feature's presentation module imports its own data module (see `clean-presentation-layer`).

Why scoped-singleton and not plain `singleton`: the service holds the refresh signal that screens
share, so there must be exactly one — and tying it to `TemplateCore` means a rebuilt core
(`TemplateCoreScope.close(core)`) takes its services down with it instead of leaving stale ones behind.

Gradle: the feature's `data` module needs `implementation(projects.clientKit.coreData)` and
`implementation(projects.clientKit.coreDomain)`; `core/data` needs the `infrastructure/api-*`
modules whose services it binds.

## Failure — translate, don't wrap

`data` is where a transport failure becomes a *domain* failure. Catch what the service throws and
rethrow the feature's exception from `domain/exceptions/`:

```kotlin
override suspend fun fetch(): DiabetesRisk = try {
    service.getDiabetesRiskScore().toDiabetesRisk()
} catch (e: ClientRequestException) {
    if (e.response.status == HttpStatusCode.ServiceUnavailable) throw DiabetesRiskUnavailableException()
    else throw e
}
```

Recurring translations belong next to the mappers, as `<Feature>+Exception.kt` — same reasoning as
`<Entity>+Dto.kt`: keep the UseCase body readable.

- **No `runCatching`, no `Result<T>` crossing into `domain`.** It only relabels a throw as a value
  the caller has to unwrap.
- Translate only what someone branches on. Everything else propagates untouched to the
  ViewModel's `CoroutineExceptionHandler` — swallowing it there would hide a real bug.
- Never let a Ktor/SQL/keystore type escape this layer; that is the whole point of the mapping.

## Checklist

- [ ] no Compose, no `androidx`, no ViewModel imports
- [ ] mapper file named `<Entity>+Dto.kt` under `mappers/`, functions are pure
- [ ] repository method reads as request → call → map → return
- [ ] the `Flow` comes from an `infrastructure/api` service; `data` only maps it
- [ ] source wrapped in an `infrastructure/*` service, bound once in `core/data`'s `coreDataModule`
- [ ] service bound as `scoped(TemplateCoreScope).singleton`, resolved with `on(core).instance()`
- [ ] no source-specific type (DTO, SDK class, DB entity) crosses out of `data`
- [ ] service exceptions mapped to `domain/exceptions/`; no Ktor type escapes `data`
- [ ] no `runCatching`/`Result` handed back to `domain`
- [ ] cross-screen updates flow through a `Flow` from the service, never a context object
- [ ] implementation named `<Feature>Repository`, not `<Feature>UseCase`, and lives in `repositories/`
- [ ] `Mock*Repository` variant exists when the feature is wired into `AppEnvironment.MOCK`
