plugins {
    alias(libs.plugins.setup.multiplatform.infrastructure)
}

kotlin {
    android {
        namespace = "com.ugg.client.infrastructure.api.core"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
    }
}
