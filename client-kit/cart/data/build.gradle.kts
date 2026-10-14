plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.cart.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.cartDomain)
            implementation(projects.clientKit.coreDomain)

            // Checkout: the signed-in user and Firestore orders. api, not implementation: they are
            // in SharedCartUseCase's public constructor, so the presentation module that binds it
            // must see them (core-data brings the Firestore services along as api).
            api(projects.clientKit.coreData)
            api(projects.infrastructure.firebaseAuth)
        }
    }
}
