import java.util.Properties

plugins {
    alias(libs.plugins.setup.android.application)
}

// Secrets live in keys.properties at the repository root, which is not committed.
// Without it the app still builds: the stores map falls back to a placeholder.
val keys = Properties().apply {
    val file = rootProject.file("keys.properties")
    if (file.isFile) file.inputStream().use(::load)
}

android {
    namespace = "com.ugg.client.android"

    defaultConfig {
        applicationId = "com.ugg.client"
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "YANDEX_API_KEY", "\"${keys.getProperty("YANDEX_API_KEY", "").trim('"')}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
