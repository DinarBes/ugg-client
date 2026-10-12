# template-client v2 — Full Architecture Upgrade

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade template-client with 6 architectural patterns ported from fs-client: `infrastructure/` layer (replaces external dep), sealed Content states, navigation3, typed Destinations, AppEnvironment mocking, and OtpAuthorizationContext multi-step flow pattern.

**Architecture:** New `infrastructure/api-core` and `infrastructure/api-auth` modules replace the external `template-client-api-core`/`template-client-api-auth` Maven deps. Auth is redesigned as a 2-step flow (phone → OTP) bridged by `OtpAuthorizationContext`. Navigation3's `NavDisplay`+`NavBackStack` replaces `NavHost`+`NavController`. Every ViewModel exposes a single `StateFlow<XyzState>` with sealed inner `Content` class.

**Tech Stack:** navigation3-ui 1.0.0-alpha05, ktor-client-auth 3.4.2, multiplatform-foundation-core 1.3.2, Kodein DI 7.31.0, Kotlin Serialization 1.11.0

**Reference project:** `/Users/artur/Developer/FactorSmile/multiplatform/fs-client`

---

## File Map

### New files
```
build-logic/src/main/kotlin/setup-multiplatform-infrastructure.gradle.kts
infrastructure/api-core/build.gradle.kts
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateApi.kt
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCore.kt
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/ktor/KtorModule.kt
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/ktor/plugins/HttpClientConfig+defaultRequest.kt
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/ktor/plugins/HttpClientConfig+responseValidation.kt
infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt
infrastructure/api-auth/build.gradle.kts
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/TemplateAuth.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/context/AuthContext.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/context/AuthEvent.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/keystore/InMemoryKeystore.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/keystore/MockKeystore.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/keystore/KeystoreModule.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/services/AccessService.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/ktor/KtorModule.kt
infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/ktor/plugins/HttpClientConfig+bearerAuth.kt
client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/InitiateState.kt
client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/OtpState.kt
client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/entities/AuthFlow.kt
client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/contexts/OtpAuthorizationContext.kt
client-kit/auth/data/src/commonMain/kotlin/com/template/client/kit/auth/data/services/AuthService.kt
client-kit/auth/data/src/commonMain/kotlin/com/template/client/kit/auth/data/useCases/StubAuthUseCase.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/models/InitiateViewModel.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/models/OtpViewModel.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/InitiateScreen.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/OtpScreen.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/internal/view/InitiateView.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/internal/view/OtpView.kt
client-kit/auth/presentation/src/commonTest/kotlin/com/template/client/kit/auth/presentation/external/view/models/InitiateViewModelTest.kt
client-kit/auth/presentation/src/commonTest/kotlin/com/template/client/kit/auth/presentation/external/view/models/OtpViewModelTest.kt
```

### Files to delete
```
client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/entities/AuthCredentials.kt
client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/AuthState.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/models/AuthViewModel.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/AuthScreen.kt
client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/internal/view/AuthView.kt
```

### Modified files
```
build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts       — remove libs.template.client.api.core
settings.gradle.kts                                                        — register infrastructure modules
gradle/libs.versions.toml                                                  — add nav3/ktor, remove external api deps
shared/build.gradle.kts                                                    — export infrastructure, remove external dep
shared/src/commonMain/kotlin/com/template/client/shared/TemplateApp.kt   — updated imports
shared/src/commonMain/kotlin/com/template/client/shared/dsl/TemplateAppBuilder.kt — updated imports
client-kit/navigation/domain/build.gradle.kts                             — nav-compose → nav3
client-kit/navigation/domain/src/.../external/entities/Destination.kt    — add NavKey + isTabViewVisible
client-kit/navigation/domain/src/.../external/entities/NavigationEvent.kt — add OnBack
client-kit/navigation/domain/src/.../external/contexts/NavigationContext.kt — add back()
client-kit/navigation/presentation/build.gradle.kts                       — add nav3
client-kit/navigation/presentation/src/.../view/NavigationScreen.kt      — rewrite with NavDisplay
client-kit/splash/domain/src/.../entities/SplashFlow.kt                  — add isTabViewVisible
client-kit/splash/domain/src/.../entities/SplashDestination.kt           — add isTabViewVisible
client-kit/splash/presentation/src/.../view/SplashScreen.kt              — (SplashFlow)->Unit signature
client-kit/splash/presentation/src/.../view/models/SplashViewModel.kt    — { clear() } instead of popUpTo
client-kit/splash/data/build.gradle.kts                                   — api.auth → infrastructure.apiAuth
client-kit/splash/data/src/.../useCases/SharedSplashUseCase.kt           — updated imports
client-kit/auth/domain/src/.../useCases/AuthUseCase.kt                   — add initiate(phone): String
client-kit/auth/data/build.gradle.kts                                     — add infrastructure.apiAuth dep
client-kit/auth/data/src/.../useCases/SharedAuthUseCase.kt               — use TemplateAuth from infrastructure
client-kit/auth/presentation/build.gradle.kts                             — add infrastructure.apiAuth dep
client-kit/auth/presentation/src/.../external/AuthModule.kt              — AppEnvironment fork, new VMs
CLAUDE.md                                                                  — updated templates + new sections
```

---

## Task 1: Add navigation3 + ktor-client-auth to libs.versions.toml

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add navigation3 version and ktor-client-auth to versions.toml**

In `gradle/libs.versions.toml`, add after `androidx-navigation-version`:

```toml
# [versions] — add:
navigation3-version = "1.0.0-alpha05"
ktor-version = "3.4.2"
```

In `[libraries]` section, replace:
```toml
# Remove this line:
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "androidx-navigation-version" }

# Add these lines:
navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "navigation3-version" }
ktor-client-auth = { group = "io.ktor", name = "ktor-client-auth", version.ref = "ktor-version" }
```

Also remove these two lines (external API deps being replaced):
```toml
# Remove:
template-client-api-core = { group = "com.template", name = "client-api-core", version.ref = "template-client-api-version" }
template-client-api-auth = { group = "com.template", name = "client-api-auth", version.ref = "template-client-api-version" }
# And the version ref:
template-client-api-version = "1.0.0"
```

Also remove `androidx-navigation-version = "2.9.2"` since it's no longer needed.

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "deps: replace navigation-compose+external-api with navigation3+ktor-auth+infrastructure"
```

---

## Task 2: Create setup-multiplatform-infrastructure convention plugin

**Files:**
- Create: `build-logic/src/main/kotlin/setup-multiplatform-infrastructure.gradle.kts`

- [ ] **Step 1: Create the infrastructure convention plugin**

Create `build-logic/src/main/kotlin/setup-multiplatform-infrastructure.gradle.kts`:

```kotlin
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(AndroidConfig.JVM_TARGET.target.toInt())

    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    android {
        compileSdk = AndroidConfig.COMPILE_SDK
        minSdk = AndroidConfig.MIN_SDK

        withJava()

        compilerOptions {
            jvmTarget = AndroidConfig.JVM_TARGET
        }
    }

    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kodein.di)

            implementation(libs.multiplatform.foundation.core)
            implementation(libs.multiplatform.foundation.network)

            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

- [ ] **Step 2: Register the new plugin in build-logic/build.gradle.kts**

Read `build-logic/build.gradle.kts` to verify the `gradlePlugin` block format, then add:

```kotlin
// Inside gradlePlugin { plugins { ... } }
`setup-multiplatform-infrastructure` {
    id = "setup-multiplatform-infrastructure"
    implementationClass = "..."  // convention plugins don't need implementationClass
}
```

Actually, convention plugins defined as `.gradle.kts` files are auto-discovered — no `gradlePlugin` block entry needed. Just creating the file is enough.

- [ ] **Step 3: Add plugin alias to gradle/libs.versions.toml**

In `[plugins]` section, add:
```toml
setup-multiplatform-infrastructure = { id = "setup-multiplatform-infrastructure" }
```

- [ ] **Step 4: Commit**

```bash
git add build-logic/src/main/kotlin/setup-multiplatform-infrastructure.gradle.kts gradle/libs.versions.toml
git commit -m "build: add setup-multiplatform-infrastructure convention plugin"
```

---

## Task 3: Create infrastructure/api-core module

**Files:**
- Create all 9 files listed in the file map under `infrastructure/api-core/`

- [ ] **Step 1: Create `infrastructure/api-core/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.infrastructure)
}

kotlin {
    android {
        namespace = "com.template.client.infrastructure.api.core"
    }
}
```

- [ ] **Step 2: Create `TemplateApi.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateApi.kt`:

```kotlin
package com.template.client.infrastructure.api.core

import io.ktor.http.URLProtocol

sealed class TemplateApi {
    abstract val protocol: URLProtocol
    abstract val host: String
    abstract val path: List<String>

    sealed class Client : TemplateApi() {
        data object Localhost : Client() {
            override val protocol = URLProtocol.HTTP
            override val host = "localhost:8080"
            override val path = listOf("")
        }
    }
}
```

- [ ] **Step 3: Create `TemplateCoreConfiguration.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt`:

```kotlin
package com.template.client.infrastructure.api.core

import io.ktor.client.plugins.logging.LogLevel

class TemplateCoreConfiguration {
    var api: TemplateApi = TemplateApi.Client.Localhost
    var logLevel: LogLevel = LogLevel.INFO
}
```

- [ ] **Step 4: Create `MultiplatformCoreModule.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt`:

```kotlin
package com.template.client.infrastructure.api.core.multiplatform

import cloud.paybuddy.multiplatform.foundation.core.MultiplatformCore
import cloud.paybuddy.multiplatform.foundation.network.NetworkPlugin
import cloud.paybuddy.multiplatform.foundation.network.engine.MultiplatformEngine
import cloud.paybuddy.multiplatform.foundation.secure.KeystorePlugin
import cloud.paybuddy.multiplatform.foundation.serialization.SerializationPlugin
import com.template.client.infrastructure.api.core.TemplateCoreConfiguration
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

internal val multiplatformCoreModule: DI.Module
    get() = DI.Module("multiplatform-core") {
        bind<MultiplatformCore>() with provider {
            val configuration: TemplateCoreConfiguration = instance()

            MultiplatformCore {
                install(KeystorePlugin)

                install(NetworkPlugin) {
                    engine(MultiplatformEngine)

                    standardLogging {
                        level = configuration.logLevel
                    }
                }

                install(SerializationPlugin)
            }
        }
    }
```

- [ ] **Step 5: Create `HttpClientConfig+defaultRequest.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/ktor/plugins/HttpClientConfig+defaultRequest.kt`:

```kotlin
package com.template.client.infrastructure.api.core.ktor.plugins

import com.template.client.infrastructure.api.core.TemplateApi
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.path

internal fun HttpClientConfig<*>.templateDefaultRequest(api: TemplateApi) {
    defaultRequest {
        url {
            protocol = api.protocol
            host = api.host
            path(*api.path.toTypedArray())
        }
    }
}
```

- [ ] **Step 6: Create `HttpClientConfig+responseValidation.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/ktor/plugins/HttpClientConfig+responseValidation.kt`:

```kotlin
package com.template.client.infrastructure.api.core.ktor.plugins

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.http.isSuccess

internal fun HttpClientConfig<*>.templateResponseValidation() {
    install(HttpTimeout) {
        requestTimeoutMillis = 40_000
    }

    expectSuccess = true

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val throwable = (cause as? ResponseException) ?: return@handleResponseExceptionWithRequest
            val response = throwable.response

            if (response.status.isSuccess()) return@handleResponseExceptionWithRequest

            val result = runCatching { response.body<Exception>() }
            throw result.getOrNull() ?: Exception(throwable)
        }
    }
}
```

- [ ] **Step 7: Create `KtorModule.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/ktor/KtorModule.kt`:

```kotlin
package com.template.client.infrastructure.api.core.ktor

import cloud.paybuddy.multiplatform.foundation.core.MultiplatformCore
import cloud.paybuddy.multiplatform.foundation.network.createClient
import com.template.client.infrastructure.api.core.TemplateCoreConfiguration
import com.template.client.infrastructure.api.core.ktor.plugins.templateDefaultRequest
import com.template.client.infrastructure.api.core.ktor.plugins.templateResponseValidation
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

internal val ktorModule: DI.Module
    get() = DI.Module("ktor-module") {
        bind<HttpClient>() with provider {
            val core: MultiplatformCore = instance()
            val configuration: TemplateCoreConfiguration = instance()

            core.createClient {
                templateDefaultRequest(configuration.api)
                templateResponseValidation()
            }
        }
    }
```

- [ ] **Step 8: Create `TemplateCore.kt`**

`infrastructure/api-core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCore.kt`:

```kotlin
package com.template.client.infrastructure.api.core

import com.template.client.infrastructure.api.core.ktor.ktorModule
import com.template.client.infrastructure.api.core.multiplatform.multiplatformCoreModule
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.LazyDelegate
import org.kodein.di.bind
import org.kodein.di.instance

class TemplateCore(block: TemplateCoreConfiguration.() -> Unit) {
    val di: DI

    init {
        val configuration = TemplateCoreConfiguration()
        configuration.block()

        di = DI {
            bind<TemplateCoreConfiguration>() with instance(configuration)
            import(multiplatformCoreModule)
            import(ktorModule)
        }
    }

    val client: LazyDelegate<HttpClient>
        get() = di.instance()
}
```

- [ ] **Step 9: Commit**

```bash
git add infrastructure/api-core/
git commit -m "feat: add infrastructure/api-core module (TemplateCore, TemplateApi, Ktor setup)"
```

---

## Task 4: Create infrastructure/api-auth module

**Files:**
- Create all 9 files listed in the file map under `infrastructure/api-auth/`

- [ ] **Step 1: Create `infrastructure/api-auth/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.infrastructure)
}

kotlin {
    android {
        namespace = "com.template.client.infrastructure.api.auth"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.infrastructure.apiCore)
            api(libs.ktor.client.auth)
        }
    }
}
```

- [ ] **Step 2: Create `AuthEvent.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/context/AuthEvent.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.context

sealed class AuthEvent {
    data object Login : AuthEvent()
    data object Logout : AuthEvent()
}
```

- [ ] **Step 3: Create `AuthContext.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/context/AuthContext.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.context

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class AuthContext {
    val event: SharedFlow<AuthEvent>
        field = MutableSharedFlow()

    suspend fun send(event: AuthEvent) = this.event.emit(event)

    fun trySend(event: AuthEvent) = this.event.tryEmit(event)
}
```

- [ ] **Step 4: Create `InMemoryKeystore.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/keystore/InMemoryKeystore.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.keystore

import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore

class InMemoryKeystore : Keystore {
    private val map: MutableMap<String, String> = mutableMapOf()

    override fun get(key: String): String? = map[key]

    override fun set(key: String, value: String?) {
        if (value != null) map[key] = value else map.remove(key)
    }
}
```

- [ ] **Step 5: Create `MockKeystore.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/keystore/MockKeystore.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.keystore

import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore

class MockKeystore : Keystore {
    private val store = mutableMapOf<String, String?>()

    override fun get(key: String): String? = store[key]

    override fun set(key: String, value: String?) { store[key] = value }
}
```

- [ ] **Step 6: Create `AccessService.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/services/AccessService.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.services

import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore

class AccessService(val keystore: Keystore) {
    companion object {
        private const val ACCESS_TOKEN_KEY = "access-token"
        private const val REFRESH_TOKEN_KEY = "refresh-token"
    }

    var accessToken: String?
        get() = keystore[ACCESS_TOKEN_KEY]
        set(value) { keystore[ACCESS_TOKEN_KEY] = value }

    var refreshToken: String?
        get() = keystore[REFRESH_TOKEN_KEY]
        set(value) { keystore[REFRESH_TOKEN_KEY] = value }

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
```

- [ ] **Step 7: Create `KeystoreModule.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/keystore/KeystoreModule.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.keystore

import cloud.paybuddy.multiplatform.foundation.core.MultiplatformCore
import cloud.paybuddy.multiplatform.foundation.secure.keystore
import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore
import com.template.client.infrastructure.api.auth.services.AccessService
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance

internal val keystoreModule: DI.Module
    get() = DI.Module("keystore.module") {
        bind<Keystore>() with factory {
            instance<MultiplatformCore>().keystore
        }

        bind<AccessService>() with factory {
            AccessService(keystore = instance())
        }
    }
```

- [ ] **Step 8: Create `HttpClientConfig+bearerAuth.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/ktor/plugins/HttpClientConfig+bearerAuth.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.ktor.plugins

import com.template.client.infrastructure.api.auth.context.AuthContext
import com.template.client.infrastructure.api.auth.context.AuthEvent
import com.template.client.infrastructure.api.auth.services.AccessService
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer

internal fun HttpClientConfig<*>.templateBearerAuth(
    accessService: AccessService,
    context: AuthContext,
) {
    install(Auth) {
        bearer {
            loadTokens {
                val accessToken = accessService.accessToken ?: return@loadTokens null
                val refreshToken = accessService.refreshToken ?: return@loadTokens null
                BearerTokens(accessToken, refreshToken)
            }

            refreshTokens {
                try {
                    // TODO: implement token refresh when backend is ready
                    // val response = client.submitForm(url = "oauth2/token", ...)
                    // val tokens: TokenResponse = response.body()
                    // accessService.accessToken = tokens.accessToken
                    // accessService.refreshToken = tokens.refreshToken
                    // BearerTokens(tokens.accessToken, tokens.refreshToken)
                    null
                } catch (_: Throwable) {
                    accessService.clear()
                    context.send(AuthEvent.Logout)
                    null
                }
            }
        }
    }
}
```

- [ ] **Step 9: Create `KtorModule.kt` (auth)**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/ktor/KtorModule.kt`:

```kotlin
package com.template.client.infrastructure.api.auth.ktor

import com.template.client.infrastructure.api.auth.context.AuthContext
import com.template.client.infrastructure.api.auth.ktor.plugins.templateBearerAuth
import com.template.client.infrastructure.api.auth.services.AccessService
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindings.overriddenInstanceOf
import org.kodein.di.instance
import org.kodein.di.singleton

internal val ktorModule: DI.Module
    get() = DI.Module("ktor-auth-module", allowSilentOverride = true) {
        bind<HttpClient>() with singleton {
            val client: HttpClient = overriddenInstanceOf()
            val context: AuthContext = instance()
            val accessService: AccessService = instance()

            client.config {
                templateBearerAuth(accessService, context)
            }
        }
    }
```

- [ ] **Step 10: Create `TemplateAuth.kt`**

`infrastructure/api-auth/src/commonMain/kotlin/com/template/client/infrastructure/api/auth/TemplateAuth.kt`:

```kotlin
package com.template.client.infrastructure.api.auth

import com.template.client.infrastructure.api.auth.context.AuthContext
import com.template.client.infrastructure.api.auth.keystore.keystoreModule
import com.template.client.infrastructure.api.auth.ktor.ktorModule
import com.template.client.infrastructure.api.auth.services.AccessService
import com.template.client.infrastructure.api.core.TemplateCore
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.LazyDelegate
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class TemplateAuth(val core: TemplateCore) {
    val di: DI = DI {
        extend(core.di, allowOverride = true)

        bind<AuthContext>() with singleton { AuthContext() }

        import(keystoreModule)
        import(ktorModule, allowOverride = true)
    }

    val context: LazyDelegate<AuthContext>
        get() = di.instance()

    val access: LazyDelegate<AccessService>
        get() = di.instance()

    val client: LazyDelegate<HttpClient>
        get() = di.instance()

    fun isAuthorized(): Boolean {
        val access by access
        return access.refreshToken != null
    }
}
```

- [ ] **Step 11: Commit**

```bash
git add infrastructure/api-auth/
git commit -m "feat: add infrastructure/api-auth module (TemplateAuth, AuthContext, keystore, bearer auth)"
```

---

## Task 5: Register infrastructure modules + update shared layer

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `shared/build.gradle.kts`
- Modify: `shared/src/commonMain/kotlin/com/template/client/shared/TemplateApp.kt`
- Modify: `shared/src/commonMain/kotlin/com/template/client/shared/dsl/TemplateAppBuilder.kt`

- [ ] **Step 1: Register infrastructure modules in `settings.gradle.kts`**

Add before the `listOf(...)` block in `settings.gradle.kts`:

```kotlin
include(":infrastructure:api-core")
project(":infrastructure:api-core").projectDir = file("infrastructure/api-core")

include(":infrastructure:api-auth")
project(":infrastructure:api-auth").projectDir = file("infrastructure/api-auth")
```

(Or use the same `Module` + `listOf` pattern already present if there is a separate `listOf` for infrastructure — match the existing style.)

- [ ] **Step 2: Update `shared/build.gradle.kts`**

Replace:
```kotlin
val modules = listOf(
    libs.template.client.api.core,
    // ...
)
```

With (remove `libs.template.client.api.core`, add infrastructure projects):
```kotlin
val modules = listOf(
    projects.infrastructure.apiCore,
    projects.infrastructure.apiAuth,

    projects.clientKit.coreData,
    // ... rest unchanged
)
```

- [ ] **Step 3: Update `TemplateApp.kt` imports**

`shared/src/commonMain/kotlin/com/template/client/shared/TemplateApp.kt` — change:
```kotlin
// Remove:
import com.template.client.api.core.TemplateCore

// Add:
import com.template.client.infrastructure.api.core.TemplateCore
```

- [ ] **Step 4: Update `TemplateAppBuilder.kt` imports**

`shared/src/commonMain/kotlin/com/template/client/shared/dsl/TemplateAppBuilder.kt` — change:
```kotlin
// Remove:
import com.template.client.api.core.TemplateCore
import com.template.client.api.core.dsl.TemplateApi

// Add:
import com.template.client.infrastructure.api.core.TemplateCore
import com.template.client.infrastructure.api.core.TemplateApi
```

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts shared/build.gradle.kts shared/src/
git commit -m "build: wire infrastructure modules into shared layer"
```

---

## Task 6: Remove external dep from convention plugin + update splash/data

**Files:**
- Modify: `build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts`
- Modify: `client-kit/splash/data/build.gradle.kts`
- Modify: `client-kit/splash/data/src/commonMain/kotlin/com/template/client/kit/splash/data/useCases/SharedSplashUseCase.kt`

- [ ] **Step 1: Remove `libs.template.client.api.core` from convention plugin**

In `build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts`, remove from `commonMain.dependencies {}`:
```kotlin
// Remove this line:
implementation(libs.template.client.api.core)
```

- [ ] **Step 2: Update `splash/data/build.gradle.kts`**

Replace:
```kotlin
api(libs.template.client.api.auth)
```
With:
```kotlin
api(projects.infrastructure.apiAuth)
```

- [ ] **Step 3: Update `SharedSplashUseCase.kt` imports**

`client-kit/splash/data/src/.../SharedSplashUseCase.kt` — replace:
```kotlin
// Remove:
import com.template.client.api.auth.TemplateAuth
import com.template.client.api.auth.dtos.AuthEvent
import io.ktor.sse.SPACE  // stray import, remove

// Add:
import com.template.client.infrastructure.api.auth.TemplateAuth
import com.template.client.infrastructure.api.auth.context.AuthEvent
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :client-kit:splash-data:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts client-kit/splash/data/
git commit -m "build: replace external template-client-api deps with internal infrastructure modules"
```

---

## Task 7: Update navigation domain for navigation3

**Files:**
- Modify: `client-kit/navigation/domain/build.gradle.kts`
- Modify: `client-kit/navigation/domain/src/commonMain/kotlin/com/template/client/kit/navigation/domain/external/entities/Destination.kt`
- Modify: `client-kit/navigation/domain/src/commonMain/kotlin/com/template/client/kit/navigation/domain/external/entities/NavigationEvent.kt`
- Modify: `client-kit/navigation/domain/src/commonMain/kotlin/com/template/client/kit/navigation/domain/external/contexts/NavigationContext.kt`

- [ ] **Step 1: Update `navigation/domain/build.gradle.kts`**

Replace:
```kotlin
api(libs.androidx.navigation.compose)
```
With:
```kotlin
api(libs.navigation3.ui)
```

- [ ] **Step 2: Rewrite `Destination.kt`**

```kotlin
package com.template.client.kit.navigation.domain.external.entities

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
abstract class Destination : NavKey {
    abstract val isTabViewVisible: Boolean
}
```

- [ ] **Step 3: Rewrite `NavigationEvent.kt`**

```kotlin
package com.template.client.kit.navigation.domain.external.entities

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

sealed class NavigationEvent {
    data class OnNavigate(
        val route: Destination,
        val block: NavBackStack<NavKey>.() -> Unit,
    ) : NavigationEvent()

    data object OnBack : NavigationEvent()
}
```

- [ ] **Step 4: Rewrite `NavigationContext.kt`**

```kotlin
package com.template.client.kit.navigation.domain.external.contexts

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.template.client.kit.navigation.domain.external.entities.Destination
import com.template.client.kit.navigation.domain.external.entities.NavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NavigationContext {
    val events: SharedFlow<NavigationEvent>
        field = MutableSharedFlow(replay = 1)

    fun tryNavigate(route: Destination, block: NavBackStack<NavKey>.() -> Unit = {}) {
        events.tryEmit(NavigationEvent.OnNavigate(route, block))
    }

    suspend fun navigate(route: Destination, block: NavBackStack<NavKey>.() -> Unit = {}) {
        events.emit(NavigationEvent.OnNavigate(route, block))
    }

    fun back() {
        events.tryEmit(NavigationEvent.OnBack)
    }
}
```

- [ ] **Step 5: Verify navigation domain compiles**

```bash
./gradlew :client-kit:navigation-domain:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add client-kit/navigation/domain/
git commit -m "feat: upgrade navigation domain to navigation3 (NavKey, typed Destination, OnBack event)"
```

---

## Task 8: Update SplashFlow and SplashDestination with isTabViewVisible

**Files:**
- Modify: `client-kit/splash/domain/src/commonMain/kotlin/com/template/client/kit/splash/domain/entities/SplashFlow.kt`
- Modify: `client-kit/splash/domain/src/commonMain/kotlin/com/template/client/kit/splash/domain/entities/SplashDestination.kt`

- [ ] **Step 1: Update `SplashFlow.kt`**

```kotlin
package com.template.client.kit.splash.domain.entities

import com.template.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class SplashFlow : Destination() {
    @Serializable
    data object Auth : SplashFlow() {
        override val isTabViewVisible = false
    }

    @Serializable
    data object Launch : SplashFlow() {
        override val isTabViewVisible = false
    }
}
```

- [ ] **Step 2: Update `SplashDestination.kt`**

```kotlin
package com.template.client.kit.splash.domain.entities

import com.template.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class SplashDestination : Destination() {
    @Serializable
    data object Splash : SplashDestination() {
        override val isTabViewVisible = false
    }
}
```

- [ ] **Step 3: Verify splash domain compiles**

```bash
./gradlew :client-kit:splash-domain:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add client-kit/splash/domain/
git commit -m "feat: add isTabViewVisible to SplashFlow and SplashDestination destinations"
```

---

## Task 9: Update SplashScreen and SplashViewModel for navigation3

**Files:**
- Modify: `client-kit/splash/presentation/src/commonMain/kotlin/com/template/client/kit/splash/presentation/external/view/SplashScreen.kt`
- Modify: `client-kit/splash/presentation/src/commonMain/kotlin/com/template/client/kit/splash/presentation/external/view/models/SplashViewModel.kt`

- [ ] **Step 1: Update `SplashScreen.kt`**

```kotlin
package com.template.client.kit.splash.presentation.external.view

import androidx.compose.runtime.Composable
import com.template.client.kit.splash.domain.entities.SplashFlow
import com.template.client.kit.splash.presentation.external.view.models.SplashViewModel
import org.kodein.di.compose.viewmodel.rememberViewModel

@Composable
fun SplashScreen(
    content: @Composable (flow: SplashFlow) -> Unit,
) {
    val viewModel: SplashViewModel by rememberViewModel()
    content(viewModel.initialFlow)
}
```

- [ ] **Step 2: Update `SplashViewModel.kt`**

```kotlin
package com.template.client.kit.splash.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.client.kit.navigation.domain.external.contexts.NavigationContext
import com.template.client.kit.splash.domain.entities.SplashFlow
import com.template.client.kit.splash.domain.useCases.SplashUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

@Suppress("unused")
fun DI.splashViewModel(): SplashViewModel = this.direct.instance()

class SplashViewModel(
    splashUseCase: SplashUseCase,
    navigationContext: NavigationContext,
) : ViewModel() {
    val initialFlow: SplashFlow = splashUseCase.initialFlow()
    val flow = splashUseCase
        .flows()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialFlow)

    init {
        viewModelScope.launch {
            flow.collect {
                navigationContext.navigate(it) { clear() }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add client-kit/splash/presentation/src/
git commit -m "feat: update SplashScreen and SplashViewModel for navigation3 API"
```

---

## Task 10: Rewrite NavigationScreen with NavDisplay

**Files:**
- Modify: `client-kit/navigation/presentation/build.gradle.kts`
- Modify: `client-kit/navigation/presentation/src/commonMain/kotlin/com/template/client/kit/navigation/presentation/external/view/NavigationScreen.kt`

- [ ] **Step 1: Update `navigation/presentation/build.gradle.kts`**

Add `libs.navigation3.ui` to `commonMain.dependencies`:

```kotlin
commonMain.dependencies {
    implementation(projects.clientKit.splashDomain)
    implementation(projects.clientKit.splashPresentation)

    implementation(projects.clientKit.navigationData)
    implementation(projects.clientKit.navigationDomain)

    implementation(projects.clientKit.authDomain)
    implementation(projects.clientKit.authPresentation)

    implementation(libs.navigation3.ui)
}
```

- [ ] **Step 2: Rewrite `NavigationScreen.kt`**

```kotlin
package com.template.client.kit.navigation.presentation.external.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.template.client.kit.auth.domain.entities.AuthFlow
import com.template.client.kit.auth.presentation.external.view.InitiateScreen
import com.template.client.kit.auth.presentation.external.view.OtpScreen
import com.template.client.kit.navigation.domain.external.contexts.NavigationContext
import com.template.client.kit.navigation.domain.external.entities.NavigationEvent
import com.template.client.kit.splash.domain.entities.SplashFlow
import com.template.client.kit.splash.presentation.external.view.SplashScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.kodein.di.compose.localDI
import org.kodein.di.instance

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun NavigationScreen() {
    val di = localDI()
    val navigationContext: NavigationContext by di.instance()

    SplashScreen { flow ->
        val config = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<SplashFlow>()
                    subclassesOfSealed<AuthFlow>()
                }
            }
        }

        val backStack = rememberNavBackStack(config, flow)

        LaunchedEffect(Unit) {
            navigationContext.events.collect { event ->
                when (event) {
                    is NavigationEvent.OnNavigate -> {
                        with(event) { backStack.block() }
                        backStack.add(event.route)
                    }
                    is NavigationEvent.OnBack -> backStack.removeLastOrNull()
                }
            }
        }

        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<SplashFlow.Auth> {
                    InitiateScreen()
                }
                entry<SplashFlow.Launch> {
                    Text("Welcome! Replace with HomeScreen")
                }
                entry<AuthFlow.Otp> {
                    OtpScreen()
                }
            }
        )
    }
}
```

Note: `subclassesOfSealed<T>()` is a navigation3 extension function from `navigation3-ui`.

- [ ] **Step 3: Verify navigation presentation compiles (after auth changes in Task 13)**

Skip this build check — `InitiateScreen` and `OtpScreen` don't exist yet. This task will compile fully after Task 13.

- [ ] **Step 4: Commit**

```bash
git add client-kit/navigation/presentation/
git commit -m "feat: rewrite NavigationScreen with NavDisplay (navigation3)"
```

---

## Task 11: Redesign auth domain

**Files:**
- Delete: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/entities/AuthCredentials.kt`
- Delete: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/AuthState.kt`
- Modify: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/useCases/AuthUseCase.kt`
- Create: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/InitiateState.kt`
- Create: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/OtpState.kt`
- Create: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/entities/AuthFlow.kt`
- Create: `client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/contexts/OtpAuthorizationContext.kt`

- [ ] **Step 1: Delete old files**

```bash
rm client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/entities/AuthCredentials.kt
rm client-kit/auth/domain/src/commonMain/kotlin/com/template/client/kit/auth/domain/states/AuthState.kt
```

- [ ] **Step 2: Update `AuthUseCase.kt`**

```kotlin
package com.template.client.kit.auth.domain.useCases

interface AuthUseCase {
    suspend fun initiate(phone: String): String
    suspend fun login(otp: String, sessionId: String)
}
```

- [ ] **Step 3: Create `InitiateState.kt`**

```kotlin
package com.template.client.kit.auth.domain.states

data class InitiateState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

- [ ] **Step 4: Create `OtpState.kt`**

```kotlin
package com.template.client.kit.auth.domain.states

data class OtpState(
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

- [ ] **Step 5: Create `AuthFlow.kt`**

```kotlin
package com.template.client.kit.auth.domain.entities

import com.template.client.kit.navigation.domain.external.entities.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("destination")
sealed class AuthFlow : Destination() {
    @Serializable
    data object Otp : AuthFlow() {
        override val isTabViewVisible = false
    }
}
```

- [ ] **Step 6: Create `OtpAuthorizationContext.kt`**

```kotlin
package com.template.client.kit.auth.domain.contexts

class OtpAuthorizationContext {
    var phone: String = ""
    lateinit var sessionId: String
}
```

- [ ] **Step 7: Verify auth domain compiles**

```bash
./gradlew :client-kit:auth-domain:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add client-kit/auth/domain/
git commit -m "feat: redesign auth domain — 2-step flow (InitiateState/OtpState), AuthFlow destinations, OtpAuthorizationContext"
```

---

## Task 12: Redesign auth data layer

**Files:**
- Modify: `client-kit/auth/data/build.gradle.kts`
- Create: `client-kit/auth/data/src/commonMain/kotlin/com/template/client/kit/auth/data/services/AuthService.kt`
- Create: `client-kit/auth/data/src/commonMain/kotlin/com/template/client/kit/auth/data/useCases/StubAuthUseCase.kt`
- Modify: `client-kit/auth/data/src/commonMain/kotlin/com/template/client/kit/auth/data/useCases/SharedAuthUseCase.kt`

- [ ] **Step 1: Update `auth/data/build.gradle.kts`**

Add `projects.infrastructure.apiAuth` to `commonMain.dependencies`:

```kotlin
commonMain.dependencies {
    implementation(projects.clientKit.authDomain)
    implementation(projects.infrastructure.apiAuth)
}
```

- [ ] **Step 2: Create `AuthService.kt`**

```kotlin
package com.template.client.kit.auth.data.services

import com.template.client.infrastructure.api.auth.TemplateAuth
import com.template.client.infrastructure.api.auth.context.AuthEvent

class AuthService(private val auth: TemplateAuth) {
    private val client by auth.client
    private val access by auth.access
    private val context by auth.context

    suspend fun initiate(phone: String): String {
        // TODO: replace stub with real API call when backend is ready
        // Example:
        // val response = client.post("api/auth/initiate") {
        //     contentType(ContentType.Application.Json)
        //     setBody(mapOf("phone" to phone))
        // }
        // return response.body<InitiateResponse>().sessionId
        return "stub-session-id"
    }

    suspend fun login(otp: String, sessionId: String) {
        // TODO: replace stub with real API call when backend is ready
        // Example:
        // val response = client.submitForm(url = "oauth2/token", formParameters = Parameters.build {
        //     append("grant_type", "...")
        //     append("session_id", sessionId)
        //     append("code", otp)
        // })
        // val tokens: TokenResponse = response.body()
        // access.accessToken = tokens.accessToken
        // access.refreshToken = tokens.refreshToken
        context.trySend(AuthEvent.Login)
    }
}
```

- [ ] **Step 3: Create `StubAuthUseCase.kt`**

```kotlin
package com.template.client.kit.auth.data.useCases

import com.template.client.kit.auth.domain.useCases.AuthUseCase

class StubAuthUseCase : AuthUseCase {
    override suspend fun initiate(phone: String): String = "stub-session-id"

    override suspend fun login(otp: String, sessionId: String) {
        if (otp != "123456") throw Exception("Invalid OTP. Use 123456 in MOCK mode.")
    }
}
```

- [ ] **Step 4: Rewrite `SharedAuthUseCase.kt`**

```kotlin
package com.template.client.kit.auth.data.useCases

import com.template.client.infrastructure.api.auth.TemplateAuth
import com.template.client.kit.auth.data.services.AuthService
import com.template.client.kit.auth.domain.useCases.AuthUseCase

class SharedAuthUseCase(auth: TemplateAuth) : AuthUseCase {
    private val authService = AuthService(auth)

    override suspend fun initiate(phone: String): String = authService.initiate(phone)

    override suspend fun login(otp: String, sessionId: String) = authService.login(otp, sessionId)
}
```

- [ ] **Step 5: Verify auth data compiles**

```bash
./gradlew :client-kit:auth-data:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add client-kit/auth/data/
git commit -m "feat: redesign auth data layer — AuthService stub, StubAuthUseCase (MOCK), SharedAuthUseCase (PRODUCTION)"
```

---

## Task 13: Redesign auth presentation (TDD)

**Files:**
- Modify: `client-kit/auth/presentation/build.gradle.kts`
- Modify: `client-kit/auth/presentation/src/.../AuthModule.kt`
- Delete: old AuthViewModel, AuthScreen, AuthView
- Create: InitiateViewModel, OtpViewModel, InitiateScreen, OtpScreen, InitiateView, OtpView
- Create: InitiateViewModelTest, OtpViewModelTest

- [ ] **Step 1: Write failing tests first — `InitiateViewModelTest.kt`**

Create `client-kit/auth/presentation/src/commonTest/kotlin/com/template/client/kit/auth/presentation/external/view/models/InitiateViewModelTest.kt`:

```kotlin
package com.template.client.kit.auth.presentation.external.view.models

import com.template.client.kit.auth.domain.contexts.OtpAuthorizationContext
import com.template.client.kit.auth.domain.states.InitiateState
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import com.template.client.kit.navigation.domain.external.contexts.NavigationContext
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitiateViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialStateIsEmpty() = runTest {
        val vm = makeVm(SuccessUseCase())
        assertEquals(InitiateState(), vm.state.value)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun onSubmitSetsLoadingThenNavigatesToOtp() = runTest {
        val context = OtpAuthorizationContext()
        val vm = makeVm(SuccessUseCase(), context)
        vm.onStateChange(vm.state.value.copy(phone = "79001234567"))
        vm.onSubmit()
        assertTrue(vm.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
        assertEquals("79001234567", context.phone)
        assertEquals("stub-session-id", context.sessionId)
    }

    @Test
    fun onSubmitSetsErrorOnFailure() = runTest {
        val vm = makeVm(FailureUseCase())
        vm.onSubmit()
        advanceUntilIdle()
        assertEquals("network error", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    private fun makeVm(
        useCase: AuthUseCase,
        otpContext: OtpAuthorizationContext = OtpAuthorizationContext(),
    ) = InitiateViewModel(useCase, otpContext, NavigationContext())

    private class SuccessUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = "stub-session-id"
        override suspend fun login(otp: String, sessionId: String) = Unit
    }

    private class FailureUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = throw Exception("network error")
        override suspend fun login(otp: String, sessionId: String) = Unit
    }
}
```

- [ ] **Step 2: Write failing tests — `OtpViewModelTest.kt`**

Create `client-kit/auth/presentation/src/commonTest/kotlin/com/template/client/kit/auth/presentation/external/view/models/OtpViewModelTest.kt`:

```kotlin
package com.template.client.kit.auth.presentation.external.view.models

import com.template.client.kit.auth.domain.contexts.OtpAuthorizationContext
import com.template.client.kit.auth.domain.states.OtpState
import com.template.client.kit.auth.domain.useCases.AuthUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtpViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialStateIsEmpty() = runTest {
        val vm = makeVm(SuccessUseCase())
        assertEquals(OtpState(), vm.state.value)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun onSubmitLogsInOnSuccess() = runTest {
        val vm = makeVm(SuccessUseCase())
        vm.onStateChange(vm.state.value.copy(otp = "123456"))
        vm.onSubmit()
        assertTrue(vm.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun onSubmitSetsErrorOnFailure() = runTest {
        val vm = makeVm(FailureUseCase())
        vm.onStateChange(vm.state.value.copy(otp = "000000"))
        vm.onSubmit()
        advanceUntilIdle()
        assertEquals("Invalid OTP", vm.state.value.error)
    }

    private fun makeVm(useCase: AuthUseCase): OtpViewModel {
        val ctx = OtpAuthorizationContext()
        ctx.phone = "79001234567"
        ctx.sessionId = "stub-session-id"
        return OtpViewModel(useCase, ctx)
    }

    private class SuccessUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = "stub-session-id"
        override suspend fun login(otp: String, sessionId: String) = Unit
    }

    private class FailureUseCase : AuthUseCase {
        override suspend fun initiate(phone: String): String = "stub-session-id"
        override suspend fun login(otp: String, sessionId: String) = throw Exception("Invalid OTP")
    }
}
```

- [ ] **Step 3: Run tests — expect FAIL (classes don't exist yet)**

```bash
./gradlew :client-kit:auth-presentation:test
```
Expected: FAIL with "Unresolved reference: InitiateViewModel" and "Unresolved reference: OtpViewModel"

- [ ] **Step 4: Update `auth/presentation/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.auth.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.authData)
            implementation(projects.clientKit.authDomain)
            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.navigationDomain)
            implementation(projects.infrastructure.apiAuth)
        }
    }
}
```

- [ ] **Step 5: Delete old files**

```bash
rm client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/models/AuthViewModel.kt
rm client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/external/view/AuthScreen.kt
rm client-kit/auth/presentation/src/commonMain/kotlin/com/template/client/kit/auth/presentation/internal/view/AuthView.kt
```

- [ ] **Step 6: Create `InitiateViewModel.kt`**

```kotlin
package com.template.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.client.kit.auth.domain.contexts.OtpAuthorizationContext
import com.template.client.kit.auth.domain.entities.AuthFlow
import com.template.client.kit.auth.domain.states.InitiateState
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import com.template.client.kit.navigation.domain.external.contexts.NavigationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InitiateViewModel(
    private val authUseCase: AuthUseCase,
    private val otpContext: OtpAuthorizationContext,
    private val navigationContext: NavigationContext,
) : ViewModel() {

    val state: StateFlow<InitiateState>
        field = MutableStateFlow(InitiateState())

    fun onStateChange(new: InitiateState) { state.value = new }

    fun onSubmit() = viewModelScope.launch {
        state.update { it.copy(isLoading = true, error = null) }
        try {
            val sessionId = authUseCase.initiate(state.value.phone)
            otpContext.phone = state.value.phone
            otpContext.sessionId = sessionId
            navigationContext.navigate(AuthFlow.Otp)
        } catch (e: Throwable) {
            state.update { it.copy(error = e.message ?: "Unknown error") }
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }
}
```

- [ ] **Step 7: Create `OtpViewModel.kt`**

```kotlin
package com.template.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.client.kit.auth.domain.contexts.OtpAuthorizationContext
import com.template.client.kit.auth.domain.states.OtpState
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel(
    private val authUseCase: AuthUseCase,
    private val otpContext: OtpAuthorizationContext,
) : ViewModel() {

    val state: StateFlow<OtpState>
        field = MutableStateFlow(OtpState())

    fun onStateChange(new: OtpState) { state.value = new }

    fun onSubmit() = viewModelScope.launch {
        state.update { it.copy(isLoading = true, error = null) }
        try {
            authUseCase.login(state.value.otp, otpContext.sessionId)
        } catch (e: Throwable) {
            state.update { it.copy(error = e.message ?: "Unknown error") }
        } finally {
            state.update { it.copy(isLoading = false) }
        }
    }
}
```

- [ ] **Step 8: Create `InitiateView.kt`**

```kotlin
package com.template.client.kit.auth.presentation.internal.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.template.client.kit.auth.presentation.external.view.models.InitiateViewModel

@Composable
internal fun InitiateView(viewModel: InitiateViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.phone,
            onValueChange = { viewModel.onStateChange(state.copy(phone = it)) },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let { Text(text = it, color = Color.Red) }

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}
```

- [ ] **Step 9: Create `OtpView.kt`**

```kotlin
package com.template.client.kit.auth.presentation.internal.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.template.client.kit.auth.presentation.external.view.models.OtpViewModel

@Composable
internal fun OtpView(viewModel: OtpViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.otp,
            onValueChange = { viewModel.onStateChange(state.copy(otp = it)) },
            label = { Text("OTP Code") },
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let { Text(text = it, color = Color.Red) }

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify")
            }
        }
    }
}
```

- [ ] **Step 10: Create `InitiateScreen.kt`**

```kotlin
package com.template.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import com.template.client.kit.auth.presentation.external.view.models.InitiateViewModel
import com.template.client.kit.auth.presentation.internal.view.InitiateView
import org.kodein.di.compose.viewmodel.rememberViewModel

@Composable
fun InitiateScreen() {
    val viewModel: InitiateViewModel by rememberViewModel()
    InitiateView(viewModel)
}
```

- [ ] **Step 11: Create `OtpScreen.kt`**

```kotlin
package com.template.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import com.template.client.kit.auth.presentation.external.view.models.OtpViewModel
import com.template.client.kit.auth.presentation.internal.view.OtpView
import org.kodein.di.compose.viewmodel.rememberViewModel

@Composable
fun OtpScreen() {
    val viewModel: OtpViewModel by rememberViewModel()
    OtpView(viewModel)
}
```

- [ ] **Step 12: Rewrite `AuthModule.kt`**

```kotlin
package com.template.client.kit.auth.presentation.external

import com.template.client.infrastructure.api.auth.TemplateAuth
import com.template.client.kit.auth.data.useCases.SharedAuthUseCase
import com.template.client.kit.auth.data.useCases.StubAuthUseCase
import com.template.client.kit.auth.domain.contexts.OtpAuthorizationContext
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import com.template.client.kit.auth.presentation.external.view.models.InitiateViewModel
import com.template.client.kit.auth.presentation.external.view.models.OtpViewModel
import com.template.client.kit.core.domain.AppEnvironment
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.singleton

val authModule: DI.Module
    get() = DI.Module("auth.module") {
        bind<TemplateAuth>() with singleton {
            TemplateAuth(instance())
        }

        bind<AuthUseCase>() with factory {
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> SharedAuthUseCase(instance())
                AppEnvironment.MOCK -> StubAuthUseCase()
            }
        }

        bind<OtpAuthorizationContext>() with singleton {
            OtpAuthorizationContext()
        }

        bind<InitiateViewModel>() with factory {
            InitiateViewModel(instance(), instance(), instance())
        }

        bind<OtpViewModel>() with factory {
            OtpViewModel(instance(), instance())
        }
    }
```

- [ ] **Step 13: Run tests — expect PASS**

```bash
./gradlew :client-kit:auth-presentation:test
```
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 14: Commit**

```bash
git add client-kit/auth/presentation/
git commit -m "feat: redesign auth presentation — 2-step flow (InitiateViewModel+OtpViewModel), AppEnvironment DI fork, TDD-verified"
```

---

## Task 14: Full build verification

- [ ] **Step 1: Android build**

```bash
./gradlew :androidApp:build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: All tests**

```bash
./gradlew test
```
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 3: iOS XCFramework**

```bash
./gradlew :shared:assembleSharedXCFramework
```
Expected: BUILD SUCCESSFUL, `TemplateShared.xcframework` created in `shared/build/XCFrameworks/`

---

## Task 15: Update CLAUDE.md

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Update "Adding a New Feature Module" ViewModel template**

In `CLAUDE.md`, the `XyzViewModel.kt` template currently uses flat `entity/isLoading/error` StateFlows. Replace with:

```kotlin
class <Feature>ViewModel(private val useCase: <Feature>UseCase) : ViewModel() {

    val state: StateFlow<<Feature>State>
        field = MutableStateFlow(<Feature>State())

    fun load() {
        viewModelScope.launch {
            state.value = <Feature>State(content = <Feature>State.Content.Loading)
            try {
                val result = useCase.get()
                state.value = <Feature>State(content = <Feature>State.Content.Success(result))
            } catch (e: Exception) {
                state.value = <Feature>State(content = <Feature>State.Content.Failure(e.message ?: "Unknown error"))
            }
        }
    }
}
```

- [ ] **Step 2: Add `XyzState.kt` to the 11 files in Step 1**

Add a new file to the feature module template: `<Feature>State.kt` in `domain/states/`:

```kotlin
package <pkg>.kit.<feature>.domain.states

data class <Feature>State(val content: Content = Content.Loading) {
    sealed class Content {
        data object Loading : Content()
        data class Success(val entity: <Feature>Entity) : Content()
        data class Failure(val message: String) : Content()
    }
}
```

- [ ] **Step 3: Update the DI module template with AppEnvironment fork**

Replace `XyzModule.kt` template:
```kotlin
val <feature>Module: DI.Module
    get() = DI.Module("<feature>.module") {
        bind<<Feature>UseCase>() with factory {
            when (instance<AppEnvironment>()) {
                AppEnvironment.PRODUCTION -> Shared<Feature>UseCase(instance())
                AppEnvironment.MOCK -> Stub<Feature>UseCase()
            }
        }
        bind<<Feature>ViewModel>() with factory { <Feature>ViewModel(instance()) }
    }
```

And rename the data layer stub:
```kotlin
// Stub<Feature>UseCase.kt — used for MOCK env
class Stub<Feature>UseCase : <Feature>UseCase {
    override suspend fun get(): <Feature>Entity = <Feature>Entity(id = "stub")
}

// Shared<Feature>UseCase.kt — used for PRODUCTION env
class Shared<Feature>UseCase(/* inject API client */) : <Feature>UseCase {
    override suspend fun get(): <Feature>Entity {
        // TODO: implement real API call
        throw NotImplementedError("Replace with real implementation")
    }
}
```

- [ ] **Step 4: Update ViewModel test template**

Replace test assertions to use state.value.content:

```kotlin
@Test
fun loadSetsSuccessStateOnSuccess() = runTest {
    val vm = <Feature>ViewModel(SuccessUseCase())
    vm.load()
    advanceUntilIdle()
    assertIs<<Feature>State.Content.Success>(vm.state.value.content)
}

@Test
fun loadSetsFailureStateOnFailure() = runTest {
    val vm = <Feature>ViewModel(FailureUseCase())
    vm.load()
    advanceUntilIdle()
    val content = vm.state.value.content
    assertIs<<Feature>State.Content.Failure>(content)
    assertEquals("stub error", content.message)
}
```

- [ ] **Step 5: Add "Adding a Multi-step Flow" section**

Add after "Adding a New Feature Module":

```markdown
## Adding a Multi-step Flow

When a feature requires multiple screens to share ephemeral state (e.g. auth: phone → OTP), use a Context object as a bridge between ViewModels.

**Step 1 — Create the context in `<feature>/domain/contexts/`:**
```kotlin
class <Feature>FlowContext {
    var inputData: String = ""
    lateinit var serverResponse: String
}
```

**Step 2 — Bind as singleton in the DI module:**
```kotlin
bind<<Feature>FlowContext>() with singleton { <Feature>FlowContext() }
```

**Step 3 — Step-1 ViewModel writes to context, then navigates:**
```kotlin
class Step1ViewModel(
    private val useCase: ...,
    private val context: <Feature>FlowContext,
    private val nav: NavigationContext,
) : ViewModel() {
    fun onSubmit() = viewModelScope.launch {
        val response = useCase.doStep1(state.value.input)
        context.inputData = state.value.input
        context.serverResponse = response
        nav.navigate(<Feature>Destination.Step2)
    }
}
```

**Step 4 — Step-2 ViewModel reads from context:**
```kotlin
class Step2ViewModel(
    private val useCase: ...,
    private val context: <Feature>FlowContext,
) : ViewModel() {
    fun onSubmit() = viewModelScope.launch {
        useCase.doStep2(state.value.code, context.serverResponse)
    }
}
```

See `client-kit/auth/` for a complete working example.
```

- [ ] **Step 6: Add "Infrastructure Layer" section**

Add after "Dependency Injection":

```markdown
## Infrastructure Layer

`infrastructure/api-core` and `infrastructure/api-auth` provide the HTTP client foundation.

**TemplateCore** — Ktor HttpClient + serialization:
```kotlin
// shared/TemplateAppBuilder.kt
var core = TemplateCore {
    api = TemplateApi.Client.Localhost  // change to your server
    logLevel = LogLevel.INFO
}
```

**TemplateAuth** — extends core DI with bearer token auth:
```kotlin
// Bound in AuthModule, injected into data-layer UseCases
bind<TemplateAuth>() with singleton { TemplateAuth(instance()) }
```

**Adding a new API service module** (e.g. `infrastructure/api-profile`):
1. Create module with `setup.multiplatform.infrastructure` plugin
2. Declare `implementation(projects.infrastructure.apiAuth)` dep
3. Create `ProfileService(auth: TemplateAuth)` — call `auth.client` for HTTP
4. Register your service in the relevant feature `DI.Module`
```

- [ ] **Step 7: Update "Current State" block**

```markdown
## Current State

Authentication uses a 2-step flow (phone → OTP). In `MOCK` env (`AppEnvironment.MOCK`), `StubAuthUseCase` accepts any phone and OTP `123456`. In `PRODUCTION` env, `SharedAuthUseCase` delegates to `AuthService` — replace the stub bodies in `AuthService.kt` with real Ktor calls when the backend is ready. No other changes needed — the DI binding in `AuthModule` switches implementations automatically.
```

- [ ] **Step 8: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md — new feature module templates (sealed state, AppEnvironment), multi-step flow guide, infrastructure layer guide"
```
