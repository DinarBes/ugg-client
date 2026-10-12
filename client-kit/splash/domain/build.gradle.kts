plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.splash.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.navigationDomain)
        }
    }
}
