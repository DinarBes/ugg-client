plugins {
    alias(libs.plugins.setup.multiplatform.library)
}

kotlin {
    android {
        namespace = "com.ugg.client.infrastructure.keystore.session"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.infrastructure.keystoreFoundation)
        }
    }
}
