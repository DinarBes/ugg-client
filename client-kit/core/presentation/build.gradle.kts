plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.core.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.coreData)
            implementation(projects.clientKit.coreDomain)
        }
    }
}

compose.resources {
    packageOfResClass = "com.ugg.client.kit.core.presentation.resources"
}
