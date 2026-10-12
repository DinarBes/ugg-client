plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.android)
    implementation(libs.gradle.compose)
    implementation(libs.gradle.compose.compiler)
    implementation(libs.gradle.kotlin.serialization)
    implementation(libs.gradle.skie)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
