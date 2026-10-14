plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.profile.domain"
    }

    sourceSets {
        commonMain.dependencies {
            // api: ProfileUseCase exposes a Flow.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
