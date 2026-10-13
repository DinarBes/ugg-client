rootProject.name = "ugg-client"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

data class Module(val name: String, val path: String)

includeBuild("build-logic")

include(":shared")
include(":androidApp")

listOf(
    Module(":infrastructure:api-core", "infrastructure/api/core"),
    Module(":infrastructure:api-auth", "infrastructure/api/auth"),
    Module(":infrastructure:keystore-foundation", "infrastructure/keystore/foundation"),
    Module(":infrastructure:keystore-session", "infrastructure/keystore/session"),

    Module(":client-kit:core-data", "client-kit/core/data"),
    Module(":client-kit:core-domain", "client-kit/core/domain"),
    Module(":client-kit:core-presentation", "client-kit/core/presentation"),

    Module(":client-kit:navigation-data", "client-kit/navigation/data"),
    Module(":client-kit:navigation-domain", "client-kit/navigation/domain"),
    Module(":client-kit:navigation-presentation", "client-kit/navigation/presentation"),

    Module(":client-kit:auth-data", "client-kit/auth/data"),
    Module(":client-kit:auth-domain", "client-kit/auth/domain"),
    Module(":client-kit:auth-presentation", "client-kit/auth/presentation"),

    Module(":client-kit:splash-data", "client-kit/splash/data"),
    Module(":client-kit:splash-domain", "client-kit/splash/domain"),
    Module(":client-kit:splash-presentation", "client-kit/splash/presentation"),

    Module(":client-kit:onboarding-data", "client-kit/onboarding/data"),
    Module(":client-kit:onboarding-domain", "client-kit/onboarding/domain"),
    Module(":client-kit:onboarding-presentation", "client-kit/onboarding/presentation"),
).forEach {
    include(it.name)
    project(it.name).projectDir = file(it.path)
}
