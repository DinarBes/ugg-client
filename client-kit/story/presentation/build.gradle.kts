plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.story.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.storyData)
            implementation(projects.clientKit.storyDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.corePresentation)
        }
    }
}
