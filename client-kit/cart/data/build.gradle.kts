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
        }
    }
}
