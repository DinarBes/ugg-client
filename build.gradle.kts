plugins {
    alias(libs.plugins.setup.multiplatform.app) apply false
    alias(libs.plugins.setup.android.application) apply false
    alias(libs.plugins.setup.multiplatform.library) apply false
}

allprojects {
    group = "com.ugg"
    version = "1.0.0"

    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}
