plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.profile.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.profileData)
            implementation(projects.clientKit.profileDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.corePresentation)
        }
    }
}
