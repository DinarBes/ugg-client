import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("setup-multiplatform-library")
}

kotlin {
    sourceSets {
        // Plain Ktor rather than the multiplatform-foundation wrappers: the web client builds every
        // infrastructure module for wasmJs, and Ktor publishes all its artifacts for it. Each
        // platform gets exactly one engine, so `HttpClient { }` picks it up without expect/actual.
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
