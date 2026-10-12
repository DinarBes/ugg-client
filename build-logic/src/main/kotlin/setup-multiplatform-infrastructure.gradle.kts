import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("setup-multiplatform-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.multiplatform.foundation.core)
            api(libs.multiplatform.foundation.network)
            api(libs.multiplatform.foundation.keystore)
            api(libs.multiplatform.foundation.serialization)
        }
    }
}
