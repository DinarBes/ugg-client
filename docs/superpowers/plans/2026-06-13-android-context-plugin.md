# Android Context Plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert `TemplateCoreConfiguration` to an `expect/actual` class so Android can inject `AndroidContextPlugin` into `MultiplatformCore`, matching the `fs-client` pattern.

**Architecture:** `TemplateCoreConfiguration` splits into a `commonMain` `expect` declaration and two platform `actual` implementations — Android installs `AndroidContextPlugin` when `androidContext` is provided, Apple's implementation is a no-op. `MultiplatformCoreModule` gains one line: `with(configuration) { multiplatformSetup() }`. `MainActivity` passes `this@MainActivity` as `androidContext`.

**Tech Stack:** Kotlin Multiplatform expect/actual, `cloud.paybuddy.multiplatform.foundation.core.context.AndroidContextPlugin`, Kodein DI

---

### Task 1: Convert TemplateCoreConfiguration to expect/actual

**Files:**
- Modify: `infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt`
- Create: `infrastructure/api/core/src/androidMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.android.kt`
- Create: `infrastructure/api/core/src/appleMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.apple.kt`

- [ ] **Step 1: Replace commonMain file with expect declaration**

Replace entire content of `infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt`:

```kotlin
package com.template.client.infrastructure.api.core

import cloud.paybuddy.multiplatform.foundation.core.dsl.MultiplatformCoreBuilder
import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore
import io.ktor.client.plugins.logging.LogLevel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TemplateCoreConfiguration() {
    var api: TemplateApi
    var logLevel: LogLevel
    var keystore: Keystore?
    fun MultiplatformCoreBuilder.multiplatformSetup()
}
```

- [ ] **Step 2: Confirm the build fails (no actuals yet)**

```bash
./gradlew :infrastructure:api-core:compileKotlinIosX64 2>&1 | tail -15
```

Expected: error containing `Expected class 'TemplateCoreConfiguration' has no actual declaration`.

- [ ] **Step 3: Create the androidMain actual**

Create directory and file `infrastructure/api/core/src/androidMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.android.kt`:

```kotlin
package com.template.client.infrastructure.api.core

import android.content.Context
import cloud.paybuddy.multiplatform.foundation.core.context.AndroidContextPlugin
import cloud.paybuddy.multiplatform.foundation.core.dsl.MultiplatformCoreBuilder
import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore
import io.ktor.client.plugins.logging.LogLevel

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

- [ ] **Step 4: Create the appleMain actual**

Create directory and file `infrastructure/api/core/src/appleMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.apple.kt`:

```kotlin
package com.template.client.infrastructure.api.core

import cloud.paybuddy.multiplatform.foundation.core.dsl.MultiplatformCoreBuilder
import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore
import io.ktor.client.plugins.logging.LogLevel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateCoreConfiguration {
    actual var api: TemplateApi = TemplateApi.Client.Localhost
    actual var logLevel: LogLevel = LogLevel.INFO
    actual var keystore: Keystore? = null

    actual fun MultiplatformCoreBuilder.multiplatformSetup() { }
}
```

- [ ] **Step 5: Confirm the build compiles**

```bash
./gradlew :infrastructure:api-core:compileKotlinIosX64 :infrastructure:api-core:compileKotlinAndroid 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Run existing tests to confirm nothing broke**

```bash
./gradlew :infrastructure:api-auth:allTests 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL` — `KeystoreOverrideTest` must still pass.

- [ ] **Step 7: Commit**

```bash
git add \
  infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt \
  infrastructure/api/core/src/androidMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.android.kt \
  infrastructure/api/core/src/appleMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.apple.kt

git commit -m "refactor: convert TemplateCoreConfiguration to expect/actual"
```

---

### Task 2: Wire multiplatformSetup() in MultiplatformCoreModule

**Files:**
- Modify: `infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt`

- [ ] **Step 1: Add multiplatformSetup() call**

Replace entire content of `infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt`:

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
                with(configuration) { multiplatformSetup() }

                install(KeystorePlugin) {
                    if (configuration.keystore != null)
                        keystore = configuration.keystore
                }

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

- [ ] **Step 2: Build and run tests**

```bash
./gradlew :infrastructure:api-auth:allTests 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL` — `KeystoreOverrideTest` still passes.

- [ ] **Step 3: Commit**

```bash
git add infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt

git commit -m "feat: call multiplatformSetup() in MultiplatformCoreModule"
```

---

### Task 3: Update MainActivity to pass androidContext

**Files:**
- Modify: `androidApp/src/main/kotlin/com/template/client/android/MainActivity.kt`

- [ ] **Step 1: Add androidContext to the TemplateCore block**

Replace entire content of `androidApp/src/main/kotlin/com/template/client/android/MainActivity.kt`:

```kotlin
package com.template.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.template.client.infrastructure.api.auth.keystore.MockKeystore
import com.template.client.infrastructure.api.core.TemplateCore
import com.template.client.kit.core.domain.AppEnvironment
import com.template.client.shared.ContentView
import com.template.client.shared.TemplateApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = TemplateApp {
            environment = AppEnvironment.MOCK
            core = TemplateCore {
                androidContext = this@MainActivity
                keystore = MockKeystore()
            }
        }

        setContent {
            ContentView(app)
        }
    }
}

@Preview
@Composable
fun ContentViewAndroidPreview() {
    val app = TemplateApp()

    ContentView(app)
}
```

- [ ] **Step 2: Build the Android app**

```bash
./gradlew :androidApp:assembleDebug 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run full project build**

```bash
./gradlew build 2>&1 | tail -15
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add androidApp/src/main/kotlin/com/template/client/android/MainActivity.kt

git commit -m "feat: pass androidContext to TemplateCore in MainActivity"
```
