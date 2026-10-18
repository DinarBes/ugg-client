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
            implementation(projects.clientKit.coreData)
            implementation(projects.clientKit.corePresentation)

            // TemplateCore: the scope the Firestore services live in.
            implementation(projects.infrastructure.apiCore)
        }

        androidMain.dependencies {
            // api: StoresMapKit.initialize is called from androidApp, which only sees this module.
            api(libs.maps.mobile)
        }

        // Yandex Maps JS API v3 inside the page: DOM access for the map container.
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
