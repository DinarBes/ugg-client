plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.home.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.homeData)
            implementation(projects.clientKit.homeDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.corePresentation)
        }
    }
}
