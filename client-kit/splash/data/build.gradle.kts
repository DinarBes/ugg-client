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
        }

        commonTest.dependencies {
            implementation(projects.infrastructure.apiCore)
            implementation(projects.infrastructure.keystoreFoundation)
        }
    }
}
