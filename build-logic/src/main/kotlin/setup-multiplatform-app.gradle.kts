import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
