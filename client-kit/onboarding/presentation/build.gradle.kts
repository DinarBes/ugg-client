plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.onboarding.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.onboardingData)
            implementation(projects.clientKit.onboardingDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.corePresentation)
        }
    }
}
