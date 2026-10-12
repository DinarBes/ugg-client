# Architecture Migration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the KMM/CMP architecture from `paybuddy-client` into `template-client`, producing a working Android+iOS multiplatform project with `core`, `navigation`, and stub `auth` feature modules.

**Architecture:** Clean architecture with `data/domain/presentation` layers per feature. Convention plugins in `build-logic` subproject centralize all Gradle configuration. The `shared` module aggregates all feature modules and exposes an `TemplateShared` XCFramework for iOS.

**Tech Stack:** Kotlin Multiplatform 2.3.10, Compose Multiplatform 1.10.2, Kodein-DI 7.31.0, AndroidX Navigation Compose 2.9.2, SKIE 0.10.10, AGP 9.1.0, Gradle 9.4.0.

---

## File Map

### Created
- `.gitignore`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/libs.versions.toml` (replace existing)
- `settings.gradle.kts` (replace existing)
- `build.gradle.kts` (replace existing)
- `build-logic/settings.gradle.kts`
- `build-logic/build.gradle.kts`
- `build-logic/src/main/kotlin/AndroidConfig.kt`
- `build-logic/src/main/kotlin/setup-android-application.gradle.kts`
- `build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts`
- `androidApp/build.gradle.kts`
- `androidApp/src/main/AndroidManifest.xml`
- `androidApp/src/main/kotlin/digital/factorsmile/client/android/MainActivity.kt`
- `shared/build.gradle.kts`
- `shared/src/commonMain/kotlin/digital/factorsmile/client/shared/AppDI.kt`
- `shared/src/commonMain/kotlin/digital/factorsmile/client/shared/ContentView.kt`
- `client-kit/core/data/build.gradle.kts`
- `client-kit/core/domain/build.gradle.kts`
- `client-kit/core/presentation/build.gradle.kts`
- `client-kit/core/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/core/presentation/external/CoreModule.kt`
- `client-kit/navigation/data/build.gradle.kts`
- `client-kit/navigation/domain/build.gradle.kts`
- `client-kit/navigation/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/domain/external/entities/NavigationEvent.kt`
- `client-kit/navigation/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/domain/external/contexts/NavigationContext.kt`
- `client-kit/navigation/presentation/build.gradle.kts`
- `client-kit/navigation/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/presentation/external/NavigationModule.kt`
- `client-kit/navigation/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/presentation/external/view/NavigationScreen.kt`
- `client-kit/auth/domain/build.gradle.kts`
- `client-kit/auth/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/domain/entities/AuthCredentials.kt`
- `client-kit/auth/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/domain/useCases/AuthUseCase.kt`
- `client-kit/auth/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/domain/states/AuthDestination.kt`
- `client-kit/auth/data/build.gradle.kts`
- `client-kit/auth/data/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/data/useCases/StubAuthUseCase.kt`
- `client-kit/auth/presentation/build.gradle.kts`
- `client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/external/AuthModule.kt`
- `client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/external/view/models/AuthViewModel.kt`
- `client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/external/view/AuthScreen.kt`
- `client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/internal/view/AuthView.kt`

### Deleted
- `app/` (entire directory — replaced by `androidApp/`)

### Modified
- `gradle/wrapper/gradle-wrapper.properties` (Gradle 9.4.0)
- `gradle/libs.versions.toml` (full replacement)
- `settings.gradle.kts` (full replacement)
- `build.gradle.kts` (full replacement)

---

## Task 1: Gradle wrapper, properties, and .gitignore

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradle.properties` (replace existing)
- Create: `.gitignore`

- [ ] **Step 1: Update gradle-wrapper.properties to Gradle 9.4.0**

`gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.0-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 2: Replace gradle.properties**

`gradle.properties`:
```properties
#Kotlin
kotlin.code.style=official
kotlin.daemon.jvmargs=-Xmx3072M

#Gradle
org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.caching=true

#Android
android.nonTransitiveRClass=true
android.useAndroidX=true
```

- [ ] **Step 3: Create .gitignore**

`.gitignore`:
```
*.iml
.kotlin
.gradle
**/build/
xcuserdata
!src/**/build/
local.properties
.idea
.DS_Store
captures
.externalNativeBuild
.cxx
*.xcodeproj/*
!*.xcodeproj/project.pbxproj
!*.xcodeproj/xcshareddata/
!*.xcodeproj/project.xcworkspace/
!*.xcworkspace/contents.xcworkspacedata
**/xcshareddata/WorkspaceSettings.xcsettings
node_modules/
.worktrees/
```

- [ ] **Step 4: Commit**
```bash
git add gradle/wrapper/gradle-wrapper.properties gradle.properties .gitignore
git commit -m "chore: set up gradle wrapper 9.4.0, properties and gitignore"
```

---

## Task 2: libs.versions.toml

**Files:**
- Modify: `gradle/libs.versions.toml` (full replacement)

- [ ] **Step 1: Replace libs.versions.toml**

`gradle/libs.versions.toml`:
```toml
[versions]
agp-version = "9.1.0"
kotlin-version = "2.3.10"
kotlin-coroutines-version = "1.10.2"

kotlinx-datetime-version = "0.7.1"
kotlinx-serialization-version = "1.8.1"

material3-version = "1.10.0-alpha05"

androidx-activity-version = "1.13.0"
androidx-lifecycle-version = "2.9.6"
androidx-navigation-version = "2.9.2"

compose-multiplatform-version = "1.10.2"

androidx-test-runner-version = "1.7.0"

skie-version = "0.10.10"

template-client-api-version = "1.0.0"

kodein-version = "7.31.0"
kermit-version = "2.1.0"

multiplatform-foundation-version = "1.3.1"

[libraries]
gradle-skie = { group = "co.touchlab.skie", name = "gradle-plugin", version.ref = "skie-version" }
gradle-kotlin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin-version" }
gradle-android = { group = "com.android.tools.build", name = "gradle", version.ref = "agp-version" }
gradle-compose = { group = "org.jetbrains.compose", name = "org.jetbrains.compose.gradle.plugin", version.ref = "compose-multiplatform-version" }
gradle-compose-compiler = { group = "org.jetbrains.kotlin.plugin.compose", name = "org.jetbrains.kotlin.plugin.compose.gradle.plugin", version.ref = "kotlin-version" }
gradle-kotlin-serialization = { group = "org.jetbrains.kotlin.plugin.serialization", name = "org.jetbrains.kotlin.plugin.serialization.gradle.plugin", version.ref = "kotlin-version" }

androidx-runner = { module = "androidx.test:runner", version.ref = "androidx-test-runner-version" }

kotlin-test = { group = "org.jetbrains.kotlin", name = "kotlin-test", version.ref = "kotlin-version" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlin-coroutines-version" }

compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose-multiplatform-version" }
compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose-multiplatform-version" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "material3-version" }
compose-ui-tooling = { module = "org.jetbrains.compose.ui:ui-tooling", version.ref = "compose-multiplatform-version" }
compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose-multiplatform-version" }
compose-ui-tooling-preview = { module = "org.jetbrains.compose.ui:ui-tooling-preview", version.ref = "compose-multiplatform-version" }
compose-components-resources = { module = "org.jetbrains.compose.components:components-resources", version.ref = "compose-multiplatform-version" }

androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity-version" }
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "androidx-navigation-version" }
androidx-lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle-version" }
androidx-lifecycle-viewmodel-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidx-lifecycle-version" }

kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime-version" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization-version" }

kodein-di = { group = "org.kodein.di", name = "kodein-di", version.ref = "kodein-version" }
kodein-di-compose = { group = "org.kodein.di", name = "kodein-di-framework-compose", version.ref = "kodein-version" }

kermit = { group = "co.touchlab", name = "kermit", version.ref = "kermit-version" }

template-client-api-core = { group = "com.template", name = "client-api-core", version.ref = "template-client-api-version" }

multiplatform-foundation-core = { group = "cloud.paybuddy", name = "multiplatform-foundation-core", version.ref = "multiplatform-foundation-version" }
multiplatform-foundation-network = { group = "cloud.paybuddy", name = "multiplatform-foundation-network", version.ref = "multiplatform-foundation-version" }

[plugins]
setup-multiplatform-app = { id = "setup-multiplatform-app" }
setup-android-application = { id = "setup-android-application" }
setup-multiplatform-library = { id = "setup-multiplatform-library" }
```

- [ ] **Step 2: Commit**
```bash
git add gradle/libs.versions.toml
git commit -m "chore: replace libs.versions.toml with full KMM dependency catalog"
```

---

## Task 3: build-logic subproject

**Files:**
- Create: `build-logic/settings.gradle.kts`
- Create: `build-logic/build.gradle.kts`
- Create: `build-logic/src/main/kotlin/AndroidConfig.kt`
- Create: `build-logic/src/main/kotlin/setup-android-application.gradle.kts`
- Create: `build-logic/src/main/kotlin/setup-multiplatform-app.gradle.kts`
- Create: `build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts`

- [ ] **Step 1: Create build-logic/settings.gradle.kts**

`build-logic/settings.gradle.kts`:
```kotlin
rootProject.name = "build-logic"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

- [ ] **Step 2: Create build-logic/build.gradle.kts**

`build-logic/build.gradle.kts`:
```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.android)
    implementation(libs.gradle.compose)
    implementation(libs.gradle.compose.compiler)
    implementation(libs.gradle.kotlin.serialization)
    implementation(libs.gradle.skie)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
```

- [ ] **Step 3: Create AndroidConfig.kt**

`build-logic/src/main/kotlin/AndroidConfig.kt`:
```kotlin
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    val JVM_TARGET = JvmTarget.JVM_21
    val JAVA_VERSION = JavaVersion.VERSION_21
}
```

- [ ] **Step 4: Create setup-android-application.gradle.kts**

`build-logic/src/main/kotlin/setup-android-application.gradle.kts`:
```kotlin
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.application")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    compilerOptions {
        jvmTarget.set(AndroidConfig.JVM_TARGET)

        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    dependencies {
        implementation(project(":shared"))

        implementation(libs.androidx.activity.compose)

        implementation(libs.compose.ui.tooling.preview)
    }
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.COMPILE_SDK
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }
}
```

- [ ] **Step 5: Create setup-multiplatform-app.gradle.kts**

`build-logic/src/main/kotlin/setup-multiplatform-app.gradle.kts`:
```kotlin
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
```

- [ ] **Step 6: Create setup-multiplatform-library.gradle.kts (Android + iOS only)**

`build-logic/src/main/kotlin/setup-multiplatform-library.gradle.kts`:
```kotlin
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
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

        withHostTest {
            isIncludeAndroidResources = true
        }

        androidResources {
            enable = true
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compilerOptions {
            jvmTarget = AndroidConfig.JVM_TARGET
        }
    }

    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        @Suppress("unused")
        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.runner)
            }
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.material3)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.components.resources)

            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            implementation(libs.fs.client.api.core)

            implementation(libs.kodein.di)
            implementation(libs.kodein.di.compose)

            implementation(libs.multiplatform.foundation.core)
            implementation(libs.multiplatform.foundation.network)

            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

- [ ] **Step 7: Commit**
```bash
git add build-logic/
git commit -m "feat: add build-logic convention plugins for KMM (Android + iOS)"
```

---

## Task 4: Root Gradle configuration

**Files:**
- Modify: `settings.gradle.kts` (full replacement)
- Modify: `build.gradle.kts` (full replacement)

- [ ] **Step 1: Replace settings.gradle.kts**

`settings.gradle.kts`:
```kotlin
rootProject.name = "template-client"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

data class Module(val name: String, val path: String)

includeBuild("build-logic")

include(":shared")
include(":androidApp")

listOf(
    Module(":client-kit:core-data",               "client-kit/core/data"),
    Module(":client-kit:core-domain",             "client-kit/core/domain"),
    Module(":client-kit:core-presentation",       "client-kit/core/presentation"),

    Module(":client-kit:navigation-data",         "client-kit/navigation/data"),
    Module(":client-kit:navigation-domain",       "client-kit/navigation/domain"),
    Module(":client-kit:navigation-presentation", "client-kit/navigation/presentation"),

    Module(":client-kit:auth-data",               "client-kit/auth/data"),
    Module(":client-kit:auth-domain",             "client-kit/auth/domain"),
    Module(":client-kit:auth-presentation",       "client-kit/auth/presentation"),
).forEach {
    include(it.name)
    project(it.name).projectDir = file(it.path)
}
```

- [ ] **Step 2: Replace root build.gradle.kts**

`build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.app) apply false
    alias(libs.plugins.setup.android.application) apply false
    alias(libs.plugins.setup.multiplatform.library) apply false
}

allprojects {
    group = "com.template"
    version = "1.0.0"

    repositories {
        google()
        mavenLocal()
        mavenCentral()

        maven {
            name = "GitLabPackages"
            url = uri("https://gitlab.com/api/v4/groups/factorsmile/-/packages/maven")
            credentials {
                username = System.getenv("GITLAB_USER")
                password = System.getenv("GITLAB_TOKEN")
            }
        }
    }
}
```

- [ ] **Step 3: Verify Gradle resolves the project**

Run:
```bash
./gradlew help
```
Expected: `BUILD SUCCESSFUL` (build-logic compiles, root project settings load)

- [ ] **Step 4: Commit**
```bash
git add settings.gradle.kts build.gradle.kts
git commit -m "feat: configure root project for KMM with all modules registered"
```

---

## Task 5: androidApp module

**Files:**
- Delete: `app/` directory
- Create: `androidApp/build.gradle.kts`
- Create: `androidApp/src/main/AndroidManifest.xml`
- Create: `androidApp/src/main/kotlin/digital/factorsmile/client/android/MainActivity.kt`

- [ ] **Step 1: Delete the old :app module**

```bash
rm -rf app/
```

- [ ] **Step 2: Create androidApp/build.gradle.kts**

`androidApp/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.android.application)
}

android {
    namespace = "com.template.client.android"

    defaultConfig {
        applicationId = "com.template.client.android"
        versionCode = 1
        versionName = "1.0"
    }
}
```

- [ ] **Step 3: Create AndroidManifest.xml**

`androidApp/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:label="@string/app_name"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

- [ ] **Step 4: Create res/values/strings.xml**

`androidApp/src/main/res/values/strings.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Template</string>
</resources>
```

- [ ] **Step 5: Create MainActivity.kt**

`androidApp/src/main/kotlin/digital/factorsmile/client/android/MainActivity.kt`:
```kotlin
package com.template.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.template.client.shared.ContentView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ContentView()
        }
    }
}

@Preview
@Composable
fun ContentViewAndroidPreview() {
    ContentView()
}
```

- [ ] **Step 6: Commit**
```bash
git add androidApp/
git commit -m "feat: set up androidApp entry point module"
```

---

## Task 6: Scaffold all client-kit module build.gradle.kts files

**Files:**
- Create: all `build.gradle.kts` for core, navigation, auth data/domain/presentation

- [ ] **Step 1: Create core module build files**

`client-kit/core/domain/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.core.domain"
    }
}
```

`client-kit/core/data/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.core.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.coreDomain)
        }
    }
}
```

`client-kit/core/presentation/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.core.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.coreData)
            implementation(projects.clientKit.coreDomain)
        }
    }
}
```

- [ ] **Step 2: Create navigation module build files**

`client-kit/navigation/data/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.navigation.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.navigationDomain)
        }
    }
}
```

`client-kit/navigation/domain/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.navigation.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.navigation.compose)
        }
    }
}
```

`client-kit/navigation/presentation/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.navigation.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.navigationData)
            implementation(projects.clientKit.navigationDomain)

            implementation(projects.clientKit.authDomain)
            implementation(projects.clientKit.authPresentation)

            implementation(libs.androidx.navigation.compose)
        }
    }
}
```

- [ ] **Step 3: Create auth module build files**

`client-kit/auth/domain/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.auth.domain"
    }
}
```

`client-kit/auth/data/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.template.client.kit.auth.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.authDomain)
        }
    }
}
```

`client-kit/auth/presentation/build.gradle.kts`:
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
        }
    }
}
```

- [ ] **Step 4: Commit**
```bash
git add client-kit/
git commit -m "feat: scaffold all client-kit module build.gradle.kts files"
```

---

## Task 7: shared module

**Files:**
- Create: `shared/build.gradle.kts`
- Create: `shared/src/commonMain/kotlin/digital/factorsmile/client/shared/AppDI.kt`
- Create: `shared/src/commonMain/kotlin/digital/factorsmile/client/shared/ContentView.kt`

- [ ] **Step 1: Create shared/build.gradle.kts**

`shared/build.gradle.kts`:
```kotlin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.setup.multiplatform.library)
    id("co.touchlab.skie")
}

kotlin {
    android {
        namespace = "com.template.client.shared"
    }

    val frameworkName = "TemplateShared"
    val xcFramework = XCFramework(frameworkName)
    val modules = listOf(
        projects.clientKit.coreData,
        projects.clientKit.coreDomain,
        projects.clientKit.corePresentation,

        projects.clientKit.navigationData,
        projects.clientKit.navigationDomain,
        projects.clientKit.navigationPresentation,

        projects.clientKit.authData,
        projects.clientKit.authDomain,
        projects.clientKit.authPresentation,
    )

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
            isStatic = false

            modules.forEach { export(it) }

            xcFramework.add(this)
        }
    }

    sourceSets.commonMain.dependencies {
        modules.forEach { api(it) }

        implementation(libs.kotlinx.datetime)
    }
}
```

- [ ] **Step 2: Create AppDI.kt**

`shared/src/commonMain/kotlin/digital/factorsmile/client/shared/AppDI.kt`:
```kotlin
package com.template.client.shared

import com.template.client.kit.auth.presentation.external.authModule
import com.template.client.kit.core.presentation.external.coreModule
import com.template.client.kit.navigation.presentation.external.navigationModule
import org.kodein.di.DI

val appDI: DI
    get() = DI {
        import(coreModule)
        import(authModule)
        import(navigationModule)
    }
```

- [ ] **Step 3: Create ContentView.kt**

`shared/src/commonMain/kotlin/digital/factorsmile/client/shared/ContentView.kt`:
```kotlin
package com.template.client.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.template.client.kit.navigation.presentation.external.view.NavigationScreen
import org.kodein.di.compose.withDI

@Composable
fun ContentView() = withDI(appDI) {
    MaterialTheme {
        NavigationScreen()
    }
}
```

- [ ] **Step 4: Commit**
```bash
git add shared/
git commit -m "feat: set up shared module with TemplateShared XCFramework and DI wiring"
```

---

## Task 8: core module Kotlin sources

**Files:**
- Create: `client-kit/core/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/core/presentation/external/CoreModule.kt`

- [ ] **Step 1: Create CoreModule.kt**

`client-kit/core/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/core/presentation/external/CoreModule.kt`:
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

- [ ] **Step 2: Commit**
```bash
git add client-kit/core/
git commit -m "feat: add core module DI module with DsCore initialization"
```

---

## Task 9: navigation module Kotlin sources

**Files:**
- Create: `NavigationEvent.kt`
- Create: `NavigationContext.kt`
- Create: `NavigationModule.kt`
- Create: `NavigationScreen.kt`

- [ ] **Step 1: Create NavigationEvent.kt**

`client-kit/navigation/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/domain/external/entities/NavigationEvent.kt`:
```kotlin
package com.template.client.kit.navigation.domain.external.entities

import androidx.navigation.NavOptionsBuilder

sealed class NavigationEvent {
    data class OnNavigate<T : Any>(
        val route: T,
        val builder: NavOptionsBuilder.() -> Unit
    ) : NavigationEvent()
}
```

- [ ] **Step 2: Create NavigationContext.kt**

`client-kit/navigation/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/domain/external/contexts/NavigationContext.kt`:
```kotlin
package com.template.client.kit.navigation.domain.external.contexts

import androidx.navigation.NavOptionsBuilder
import com.template.client.kit.navigation.domain.external.entities.NavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NavigationContext {
    val events: SharedFlow<NavigationEvent>
        field = MutableSharedFlow()

    fun <T : Any> navigate(route: T, builder: NavOptionsBuilder.() -> Unit) {
        events.tryEmit(NavigationEvent.OnNavigate(route, builder))
    }
}
```

- [ ] **Step 3: Create NavigationModule.kt**

`client-kit/navigation/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/presentation/external/NavigationModule.kt`:
```kotlin
package com.template.client.kit.navigation.presentation.external

import com.template.client.kit.navigation.domain.external.contexts.NavigationContext
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val navigationModule: DI.Module
    get() = DI.Module("navigation.module") {
        bind<NavigationContext>() with singleton {
            NavigationContext()
        }
    }
```

- [ ] **Step 4: Create NavigationScreen.kt**

`client-kit/navigation/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/navigation/presentation/external/view/NavigationScreen.kt`:
```kotlin
package com.template.client.kit.navigation.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.template.client.kit.auth.domain.states.AuthDestination
import com.template.client.kit.auth.presentation.external.view.AuthScreen
import com.template.client.kit.navigation.domain.external.contexts.NavigationContext
import com.template.client.kit.navigation.domain.external.entities.NavigationEvent
import org.kodein.di.compose.localDI
import org.kodein.di.instance

@Composable
fun NavigationScreen() {
    val di = localDI()
    val navigationContext: NavigationContext by di.instance()
    val navigationController = rememberNavController()

    LaunchedEffect(Unit) {
        navigationContext.events.collect { event ->
            when (event) {
                is NavigationEvent.OnNavigate<*> -> {
                    navigationController.navigate(route = event.route, builder = event.builder)
                }
            }
        }
    }

    NavHost(navigationController, AuthDestination.Login) {
        composable<AuthDestination.Login> {
            AuthScreen()
        }
    }
}
```

- [ ] **Step 5: Commit**
```bash
git add client-kit/navigation/
git commit -m "feat: add navigation module — NavigationContext, NavigationEvent, NavigationScreen"
```

---

## Task 10: auth domain layer

**Files:**
- Create: `AuthCredentials.kt`
- Create: `AuthUseCase.kt`
- Create: `AuthDestination.kt`

- [ ] **Step 1: Create AuthCredentials.kt**

`client-kit/auth/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/domain/entities/AuthCredentials.kt`:
```kotlin
package com.template.client.kit.auth.domain.entities

data class AuthCredentials(
    val login: String,
    val password: String
)
```

- [ ] **Step 2: Create AuthUseCase.kt**

`client-kit/auth/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/domain/useCases/AuthUseCase.kt`:
```kotlin
package com.template.client.kit.auth.domain.useCases

import com.template.client.kit.auth.domain.entities.AuthCredentials

interface AuthUseCase {
    suspend fun login(credentials: AuthCredentials): Boolean
}
```

- [ ] **Step 3: Create AuthDestination.kt**

`client-kit/auth/domain/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/domain/states/AuthDestination.kt`:
```kotlin
package com.template.client.kit.auth.domain.states

import kotlinx.serialization.Serializable

sealed class AuthDestination {
    @Serializable
    data object Login : AuthDestination()
}
```

- [ ] **Step 4: Commit**
```bash
git add client-kit/auth/domain/
git commit -m "feat: add auth domain layer — AuthCredentials, AuthUseCase, AuthDestination"
```

---

## Task 11: auth data layer

**Files:**
- Create: `StubAuthUseCase.kt`

- [ ] **Step 1: Create StubAuthUseCase.kt**

`client-kit/auth/data/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/data/useCases/StubAuthUseCase.kt`:
```kotlin
package com.template.client.kit.auth.data.useCases

import com.template.client.kit.auth.domain.entities.AuthCredentials
import com.template.client.kit.auth.domain.useCases.AuthUseCase

class StubAuthUseCase : AuthUseCase {
    override suspend fun login(credentials: AuthCredentials): Boolean {
        return credentials.login == "user" && credentials.password == "password"
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add client-kit/auth/data/
git commit -m "feat: add auth data layer — StubAuthUseCase with hardcoded credentials"
```

---

## Task 12: auth presentation layer

**Files:**
- Create: `AuthModule.kt`
- Create: `AuthViewModel.kt`
- Create: `AuthView.kt`
- Create: `AuthScreen.kt`

- [ ] **Step 1: Create AuthModule.kt**

`client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/external/AuthModule.kt`:
```kotlin
package com.template.client.kit.auth.presentation.external

import com.template.client.kit.auth.data.useCases.StubAuthUseCase
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val authModule: DI.Module
    get() = DI.Module("auth.module") {
        bind<AuthUseCase>() with singleton {
            StubAuthUseCase()
        }
    }
```

- [ ] **Step 2: Create AuthViewModel.kt**

`client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/external/view/models/AuthViewModel.kt`:
```kotlin
package com.template.client.kit.auth.presentation.external.view.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.client.kit.auth.domain.entities.AuthCredentials
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authUseCase: AuthUseCase) : ViewModel() {

    val login: StateFlow<String>
        field = MutableStateFlow("")

    val password: StateFlow<String>
        field = MutableStateFlow("")

    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val error: StateFlow<String?>
        field = MutableStateFlow(null)

    val isAuthenticated: StateFlow<Boolean>
        field = MutableStateFlow(false)

    fun onLoginChange(value: String) {
        login.value = value
    }

    fun onPasswordChange(value: String) {
        password.value = value
    }

    fun onSubmit() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            val success = authUseCase.login(
                AuthCredentials(login = login.value, password = password.value)
            )
            isLoading.value = false
            if (success) {
                isAuthenticated.value = true
            } else {
                error.value = "Invalid login or password"
            }
        }
    }
}
```

- [ ] **Step 3: Create AuthView.kt (internal UI)**

`client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/internal/view/AuthView.kt`:
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.template.client.kit.auth.presentation.external.view.models.AuthViewModel

@Composable
internal fun AuthView(viewModel: AuthViewModel) {
    val login by viewModel.login.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = login,
            onValueChange = viewModel::onLoginChange,
            label = { Text("Login") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(text = it, color = Color.Red)
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }
        }
    }
}
```

- [ ] **Step 4: Create AuthScreen.kt (public entry point)**

`client-kit/auth/presentation/src/commonMain/kotlin/digital/factorsmile/client/kit/auth/presentation/external/view/AuthScreen.kt`:
```kotlin
package com.template.client.kit.auth.presentation.external.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.template.client.kit.auth.domain.useCases.AuthUseCase
import com.template.client.kit.auth.presentation.external.view.models.AuthViewModel
import com.template.client.kit.auth.presentation.internal.view.AuthView
import org.kodein.di.compose.localDI
import org.kodein.di.instance

@Composable
fun AuthScreen() {
    val di = localDI()
    val authUseCase: AuthUseCase by di.instance()
    val viewModel: AuthViewModel = viewModel { AuthViewModel(authUseCase) }
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        HomeStubScreen()
    } else {
        AuthView(viewModel)
    }
}

@Composable
private fun HomeStubScreen() {
    androidx.compose.material3.Text("Welcome to Template!")
}
```

- [ ] **Step 5: Commit**
```bash
git add client-kit/auth/presentation/
git commit -m "feat: add auth presentation layer — AuthViewModel, AuthView, AuthScreen, AuthModule"
```

---

## Task 13: Final verification

- [ ] **Step 1: Verify Gradle project loads all modules**

Run:
```bash
./gradlew projects
```
Expected output includes:
```
+--- Project ':androidApp'
+--- Project ':client-kit:auth-data'
+--- Project ':client-kit:auth-domain'
+--- Project ':client-kit:auth-presentation'
+--- Project ':client-kit:core-data'
+--- Project ':client-kit:core-domain'
+--- Project ':client-kit:core-presentation'
+--- Project ':client-kit:navigation-data'
+--- Project ':client-kit:navigation-domain'
+--- Project ':client-kit:navigation-presentation'
+--- Project ':shared'
```

- [ ] **Step 2: Verify Android build compiles**

Run:
```bash
./gradlew :androidApp:assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit any final adjustments, then tag**

```bash
git add -A
git commit -m "chore: finalize architecture migration from paybuddy-client to template-client"
```
