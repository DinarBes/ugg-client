plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.cart.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.clientKit.navigationDomain)

            // api: CartUseCase exposes Flow, so every module that uses the cart needs coroutines.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
