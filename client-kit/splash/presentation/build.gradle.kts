plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.splash.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.splashData)
            implementation(projects.clientKit.splashDomain)

            implementation(projects.clientKit.coreDomain)

            implementation(projects.clientKit.navigationDomain)
        }
    }
}
