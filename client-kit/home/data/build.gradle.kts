plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.home.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.homeDomain)
            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.coreData)
        }
    }
}
