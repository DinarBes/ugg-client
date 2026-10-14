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
            implementation(projects.clientKit.productDomain)
            implementation(projects.clientKit.productPresentation)
            implementation(projects.clientKit.storyDomain)
            implementation(projects.clientKit.storyPresentation)
            implementation(projects.clientKit.storesPresentation)
            implementation(projects.clientKit.cartDomain)
            implementation(projects.clientKit.cartPresentation)
            implementation(projects.clientKit.profileDomain)
            implementation(projects.clientKit.profilePresentation)

            implementation(projects.clientKit.corePresentation)

            implementation(libs.navigation3.ui)
        }
    }
}
