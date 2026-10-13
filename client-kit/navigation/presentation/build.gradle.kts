plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.navigation.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.splashDomain)
            implementation(projects.clientKit.splashPresentation)

            implementation(projects.clientKit.navigationData)
            implementation(projects.clientKit.navigationDomain)

            implementation(projects.clientKit.authDomain)
            implementation(projects.clientKit.authPresentation)

            implementation(projects.clientKit.onboardingPresentation)
            implementation(projects.clientKit.homePresentation)

            implementation(projects.clientKit.corePresentation)

            implementation(libs.navigation3.ui)
        }
    }
}
