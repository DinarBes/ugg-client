plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.story.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.storyDomain)
            implementation(projects.clientKit.coreDomain)
        }
    }
}
