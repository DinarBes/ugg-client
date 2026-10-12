plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.navigation.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.navigationDomain)
        }
    }
}
