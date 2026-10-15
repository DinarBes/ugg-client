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

            // api: UggTheme.adaptiveInfo exposes WindowAdaptiveInfo / WindowSizeClass to every screen.
            api(libs.compose.material3.adaptive)
            api(libs.androidx.window.core)
        }
    }
}

compose.resources {
    packageOfResClass = "com.ugg.client.kit.core.presentation.resources"
}
