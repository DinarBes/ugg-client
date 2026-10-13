plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.core.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.coreDomain)

            implementation(projects.infrastructure.apiCore)
            implementation(projects.infrastructure.apiAuth)
            implementation(projects.infrastructure.firebaseAuth)
            implementation(projects.infrastructure.firebaseFirestore)
        }
    }
}
