plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.onboarding.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.onboardingDomain)
            implementation(projects.clientKit.coreDomain)

            // api, not implementation: Keystore is in MockOnboardingUseCase's public constructor,
            // so the presentation module that binds it must see the type.
            api(projects.infrastructure.keystoreFoundation)
        }
    }
}
