plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.cart.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.cartData)
            implementation(projects.clientKit.cartDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.corePresentation)

            // TemplateCore: the scope the Firebase services live in.
            implementation(projects.infrastructure.apiCore)
        }
    }
}
