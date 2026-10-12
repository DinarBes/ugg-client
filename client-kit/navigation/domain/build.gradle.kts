plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.navigation.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.navigation3.ui)
        }
    }
}
