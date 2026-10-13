plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.auth.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.coreData)

            implementation(projects.clientKit.authDomain)
            implementation(projects.clientKit.coreDomain)

            implementation(projects.infrastructure.apiAuth)
            implementation(projects.infrastructure.firebaseAuth)
        }
    }
}
