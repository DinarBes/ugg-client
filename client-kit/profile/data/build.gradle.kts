plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.profile.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.profileDomain)
            implementation(projects.clientKit.coreDomain)
        }
    }
}
