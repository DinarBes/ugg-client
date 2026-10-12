import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("setup-multiplatform-feature")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.material3)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.components.resources)

            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            implementation(libs.kodein.di.compose)
        }
    }
}