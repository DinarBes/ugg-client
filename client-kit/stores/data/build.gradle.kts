plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.stores.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.storesDomain)
            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.coreData)
        }
    }
}
