plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.splash.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.splashDomain)

            // api, not implementation: both appear in SharedSplashUseCase's public constructor,
            // so the presentation module must see them to resolve the DI binding.
            api(projects.infrastructure.apiAuth)
            api(projects.infrastructure.keystoreSession)
            api(projects.clientKit.onboardingDomain)
            api(projects.infrastructure.firebaseAuth)
            api(projects.infrastructure.firebaseFirestore)
            implementation(projects.infrastructure.apiCore)
        }

        commonTest.dependencies {
            implementation(projects.clientKit.onboardingData)
            implementation(projects.infrastructure.apiCore)
            implementation(projects.infrastructure.keystoreFoundation)
        }
    }
}
