package com.ugg.client.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.ugg.client.infrastructure.firebase.auth.initializeFirebaseWeb
import com.ugg.client.kit.core.domain.AppEnvironment
import com.ugg.client.kit.core.domain.AppInfo
import com.ugg.client.shared.ContentView
import com.ugg.client.shared.TemplateApp
import kotlinx.browser.document

/** Loads the IANA time-zone database into js-joda, which kotlinx-datetime uses in the browser. */
@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule : JsAny

@Suppress("unused")
private val jsJodaTz = JsJodaTimeZoneModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (!FirebaseWebConfig.isFilled) {
        println("FirebaseWebConfig: paste apiKey and appId of the Firebase Web app — Firestore and sign-in will fail")
    }

    initializeFirebaseWeb(
        apiKey = FirebaseWebConfig.API_KEY,
        authDomain = FirebaseWebConfig.AUTH_DOMAIN,
        projectId = FirebaseWebConfig.PROJECT_ID,
        storageBucket = FirebaseWebConfig.STORAGE_BUCKET,
        messagingSenderId = FirebaseWebConfig.MESSAGING_SENDER_ID,
        appId = FirebaseWebConfig.APP_ID,
    )

    val app = TemplateApp {
        environment = AppEnvironment.PRODUCTION
        appInfo = AppInfo(versionName = "1.0", versionCode = 1)
    }

    ComposeViewport(document.body!!) {
        ContentView(app)
    }
}
