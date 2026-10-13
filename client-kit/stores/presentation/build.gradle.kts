plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.stores.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.storesData)
            implementation(projects.clientKit.storesDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.corePresentation)
        }

        androidMain.dependencies {
            // api: StoresMapKit.initialize is called from androidApp, which only sees this module.
            api(libs.maps.mobile)
        }
    }
}
