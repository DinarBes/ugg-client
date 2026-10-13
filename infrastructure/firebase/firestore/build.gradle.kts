plugins {
    // Not setup-multiplatform-infrastructure: that one brings Ktor and multiplatform-foundation,
    // and this module talks to the Firebase SDK, not to an HTTP API.
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.ugg.client.infrastructure.firebase.firestore"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.firestore)
        }
    }
}
