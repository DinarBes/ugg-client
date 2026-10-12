import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(AndroidConfig.JVM_TARGET.target.toInt())

    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    android {
        compileSdk = AndroidConfig.COMPILE_SDK
        minSdk = AndroidConfig.MIN_SDK

        withJava()

        withHostTest {
            isIncludeAndroidResources = true
        }

        androidResources {
            enable = true
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compilerOptions {
            jvmTarget = AndroidConfig.JVM_TARGET
        }
    }

    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        @Suppress("unused")
        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.runner)
            }
        }

        commonMain.dependencies {
            implementation(libs.kodein.di)

            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
