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
            implementation(projects.clientKit.navigationDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.coreData)
            implementation(projects.clientKit.corePresentation)

            // TemplateCore: the scope the Firebase services live in.
            implementation(projects.infrastructure.apiCore)
            implementation(projects.infrastructure.firebaseAuth)
        }
    }
}
