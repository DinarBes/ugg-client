import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.setup.multiplatform.app)
}

kotlin {
    // Run: ./gradlew :webApp:wasmJsBrowserDevelopmentRun
    // Build for hosting: ./gradlew :webApp:wasmJsBrowserDistribution
    //   → webApp/build/dist/wasmJs/productionExecutable (what firebase.json publishes).
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("ugg")
        browser {
            commonWebpackConfig {
                outputFileName = "ugg.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)

            // Time-zone rules for kotlinx-datetime: the stores' opening hours are in Asia/Irkutsk.
            implementation(npm("@js-joda/timezone", libs.versions.js.joda.timezone.version.get()))
        }
    }
}
