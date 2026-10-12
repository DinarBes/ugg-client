import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    val JVM_TARGET = JvmTarget.JVM_21
    val JAVA_VERSION = JavaVersion.VERSION_21
}
