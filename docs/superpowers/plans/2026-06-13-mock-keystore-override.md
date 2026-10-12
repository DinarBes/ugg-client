# Mock Keystore Override Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `keystore: Keystore?` to `TemplateCoreConfiguration` so tests can inject `InMemoryKeystore` instead of the platform-native Keychain/Keystore.

**Architecture:** One new field in `TemplateCoreConfiguration`; one conditional in `MultiplatformCoreModule` that forwards the custom keystore to `KeystorePlugin` when provided. All existing wiring (`TemplateAuth`, `KeystoreModule`, `AccessService`) works unchanged — they consume `MultiplatformCore` which already wraps the keystore correctly.

**Tech Stack:** Kotlin Multiplatform, Kodein DI, multiplatform-foundation `KeystorePlugin`

---

### Task 1: Write the failing test

**Files:**
- Modify: `infrastructure/api/auth/build.gradle.kts`
- Create: `infrastructure/api/auth/src/commonTest/kotlin/com/template/client/infrastructure/api/auth/keystore/KeystoreOverrideTest.kt`

- [ ] **Step 1: Add commonTest source set to api-auth build file**

Open `infrastructure/api/auth/build.gradle.kts` and add a `commonTest` block inside `sourceSets`:

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

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
```

- [ ] **Step 2: Write the failing test**

Create the file at the path above with this content:

```kotlin
package com.template.client.infrastructure.api.auth.keystore

import com.template.client.infrastructure.api.auth.TemplateAuth
import com.template.client.infrastructure.api.auth.services.AccessService
import com.template.client.infrastructure.api.core.TemplateCore
import kotlin.test.Test
import kotlin.test.assertEquals

class KeystoreOverrideTest {

    @Test
    fun customKeystoreIsUsedWhenSet() {
        val inMemory = InMemoryKeystore()
        val core = TemplateCore {
            keystore = inMemory
        }
        val auth = TemplateAuth(core)
        val access: AccessService by auth.access

        access.accessToken = "test-token"

        assertEquals("test-token", inMemory["access-token"])
    }
}
```

- [ ] **Step 3: Run the test to confirm it fails**

```bash
./gradlew :infrastructure:api-auth:compileKotlinJvm --rerun-tasks 2>&1 | tail -20
```

Expected: compile error — `Unresolved reference: keystore` on the `TemplateCore { keystore = inMemory }` line. This confirms the feature doesn't exist yet.

---

### Task 2: Implement the keystore override

**Files:**
- Modify: `infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt`
- Modify: `infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt`

- [ ] **Step 4: Add `keystore` field to `TemplateCoreConfiguration`**

Replace the entire file content:

```kotlin
package com.template.client.infrastructure.api.core

import cloud.paybuddy.multiplatform.foundation.secure.services.Keystore
import io.ktor.client.plugins.logging.LogLevel

class TemplateCoreConfiguration {
    var api: TemplateApi = TemplateApi.Client.Localhost
    var logLevel: LogLevel = LogLevel.INFO
    var keystore: Keystore? = null
}
```

- [ ] **Step 5: Update `MultiplatformCoreModule` to use the override**

Replace `install(KeystorePlugin)` with the conditional form:

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

- [ ] **Step 6: Run the test to confirm it passes**

```bash
./gradlew :infrastructure:api-auth:test --rerun-tasks 2>&1 | tail -20
```

Expected output contains `BUILD SUCCESSFUL` and `KeystoreOverrideTest > customKeystoreIsUsedWhenSet PASSED`.

- [ ] **Step 7: Run the full build to confirm nothing regressed**

```bash
./gradlew build 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add infrastructure/api/auth/build.gradle.kts \
        infrastructure/api/auth/src/commonTest/kotlin/com/template/client/infrastructure/api/auth/keystore/KeystoreOverrideTest.kt \
        infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/TemplateCoreConfiguration.kt \
        infrastructure/api/core/src/commonMain/kotlin/com/template/client/infrastructure/api/core/multiplatform/MultiplatformCoreModule.kt

git commit -m "feat: add keystore override to TemplateCoreConfiguration"
```
