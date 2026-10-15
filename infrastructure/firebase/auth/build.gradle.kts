plugins {
    // NOT setup-multiplatform-infrastructure: that one brings Ktor, and this module talks to the
    // Firebase SDK, not to an HTTP API.
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.ugg.client.infrastructure.firebase.auth"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
        }

        // The web client talks to the Firebase JS SDK (bundled by webpack from npm).
        wasmJsMain.dependencies {
            implementation(npm("firebase", libs.versions.firebase.js.version.get()))
        }
    }
}
