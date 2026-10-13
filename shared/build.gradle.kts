import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.setup.multiplatform.feature.compose)
    id("co.touchlab.skie")
}

kotlin {
    android {
        namespace = "com.ugg.client.shared"
    }

    val frameworkName = "UggShared"
    val xcFramework = XCFramework(frameworkName)
    val modules = listOf(
        projects.infrastructure.apiCore,
        projects.infrastructure.apiAuth,
        projects.infrastructure.keystoreFoundation,
        projects.infrastructure.keystoreSession,

        projects.clientKit.coreData,
        projects.clientKit.coreDomain,
        projects.clientKit.corePresentation,

        projects.clientKit.navigationData,
        projects.clientKit.navigationDomain,
        projects.clientKit.navigationPresentation,

        projects.clientKit.authData,
        projects.clientKit.authDomain,
        projects.clientKit.authPresentation,

        projects.clientKit.splashData,
        projects.clientKit.splashDomain,
        projects.clientKit.splashPresentation,

        projects.clientKit.onboardingData,
        projects.clientKit.onboardingDomain,
        projects.clientKit.onboardingPresentation,

        projects.clientKit.homeData,
        projects.clientKit.homeDomain,
        projects.clientKit.homePresentation,

        projects.clientKit.productData,
        projects.clientKit.productDomain,
        projects.clientKit.productPresentation,

        projects.clientKit.storyData,
        projects.clientKit.storyDomain,
        projects.clientKit.storyPresentation,

        projects.clientKit.storesData,
        projects.clientKit.storesDomain,
        projects.clientKit.storesPresentation,
    )

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
            isStatic = false

            modules.forEach { export(it) }

            xcFramework.add(this)
        }
    }

    sourceSets.commonMain.dependencies {
        modules.forEach { api(it) }

        implementation(libs.kotlinx.datetime)
    }
}

skie {
    features {
        enableFlowCombineConvertorPreview = true
    }
}
