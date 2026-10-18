import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

plugins {
    alias(libs.plugins.setup.multiplatform.app)
}

// Keys live in keys.properties at the repository root, which is not committed. The web build
// turns them into a generated WebKeys object; without the file it still builds (the stores map
// falls back to a link to Yandex Maps).
val keys = Properties().apply {
    val file = rootProject.file("keys.properties")
    if (file.isFile) file.inputStream().use(::load)
}

val generateWebKeys by tasks.registering {
    val yandexJsApiKey = keys.getProperty("YANDEX_JS_API_KEY", "").trim('"')
    val outputDir = layout.buildDirectory.dir("generated/webKeys/kotlin")

    inputs.property("yandexJsApiKey", yandexJsApiKey)
    outputs.dir(outputDir)

    doLast {
        val file = outputDir.get().file("com/ugg/client/web/WebKeys.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package com.ugg.client.web
            |
            |// Generated from keys.properties by :webApp:generateWebKeys. Do not edit.
            |internal object WebKeys {
            |    const val YANDEX_JS_API_KEY = "$yandexJsApiKey"
            |}
            |""".trimMargin(),
        )
    }
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
        getByName("wasmJsMain").kotlin.srcDir(generateWebKeys)

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)

            // Time-zone rules for kotlinx-datetime: the stores' opening hours are in Asia/Irkutsk.
            implementation(npm("@js-joda/timezone", libs.versions.js.joda.timezone.version.get()))
        }
    }
}
