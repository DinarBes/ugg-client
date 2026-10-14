plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.product.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.productData)
            implementation(projects.clientKit.productDomain)

            implementation(projects.clientKit.cartDomain)

            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.coreData)
            implementation(projects.clientKit.corePresentation)

            // TemplateCore: the scope the Firestore services live in.
            implementation(projects.infrastructure.apiCore)
        }
    }
}
