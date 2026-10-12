import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("com.android.application")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    compilerOptions {
        jvmTarget.set(AndroidConfig.JVM_TARGET)

        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    dependencies {
        implementation(project(":shared"))

        implementation(libs.androidx.activity.compose)

        implementation(libs.compose.ui.tooling.preview)
    }
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.COMPILE_SDK
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }
}
