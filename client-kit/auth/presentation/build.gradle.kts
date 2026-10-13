plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.auth.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.authData)
            implementation(projects.clientKit.authDomain)
            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.navigationDomain)
            implementation(projects.infrastructure.apiCore)
            implementation(projects.infrastructure.apiAuth)

            implementation(projects.clientKit.corePresentation)
        }
    }
}
