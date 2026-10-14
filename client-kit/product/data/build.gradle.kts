plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.product.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.productDomain)
            implementation(projects.clientKit.coreDomain)
            implementation(projects.clientKit.coreData)
        }
    }
}
