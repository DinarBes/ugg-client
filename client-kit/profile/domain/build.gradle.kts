plugins {
    alias(libs.plugins.setup.multiplatform.feature)
}

kotlin {
    android {
        namespace = "com.ugg.client.kit.profile.domain"
    }

    sourceSets {
        commonMain.dependencies {
            // api: the use cases expose Flows.
            api(libs.kotlinx.coroutines.core)

            implementation(projects.clientKit.navigationDomain)
        }
    }
}
