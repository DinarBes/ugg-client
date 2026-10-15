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
include(":webApp")

listOf(
    Module(":infrastructure:api-core", "infrastructure/api/core"),
    Module(":infrastructure:api-auth", "infrastructure/api/auth"),
    Module(":infrastructure:keystore-foundation", "infrastructure/keystore/foundation"),
    Module(":infrastructure:keystore-session", "infrastructure/keystore/session"),
    Module(":infrastructure:firebase-auth", "infrastructure/firebase/auth"),
    Module(":infrastructure:firebase-firestore", "infrastructure/firebase/firestore"),

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

    Module(":client-kit:home-data", "client-kit/home/data"),
    Module(":client-kit:home-domain", "client-kit/home/domain"),
    Module(":client-kit:home-presentation", "client-kit/home/presentation"),

    Module(":client-kit:product-data", "client-kit/product/data"),
    Module(":client-kit:product-domain", "client-kit/product/domain"),
    Module(":client-kit:product-presentation", "client-kit/product/presentation"),

    Module(":client-kit:care-data", "client-kit/care/data"),
    Module(":client-kit:care-domain", "client-kit/care/domain"),
    Module(":client-kit:care-presentation", "client-kit/care/presentation"),
    Module(":client-kit:story-data", "client-kit/story/data"),
    Module(":client-kit:story-domain", "client-kit/story/domain"),
    Module(":client-kit:story-presentation", "client-kit/story/presentation"),

    Module(":client-kit:stores-data", "client-kit/stores/data"),
    Module(":client-kit:stores-domain", "client-kit/stores/domain"),
    Module(":client-kit:stores-presentation", "client-kit/stores/presentation"),

    Module(":client-kit:cart-data", "client-kit/cart/data"),
    Module(":client-kit:cart-domain", "client-kit/cart/domain"),
    Module(":client-kit:cart-presentation", "client-kit/cart/presentation"),

    Module(":client-kit:profile-data", "client-kit/profile/data"),
    Module(":client-kit:profile-domain", "client-kit/profile/domain"),
    Module(":client-kit:profile-presentation", "client-kit/profile/presentation"),
).forEach {
    include(it.name)
    project(it.name).projectDir = file(it.path)
}
