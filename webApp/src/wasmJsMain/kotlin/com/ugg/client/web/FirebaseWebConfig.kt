package com.ugg.client.web

/**
 * The Web app of the Firebase project `uggs-e27e4`: Firebase console → Project settings →
 * Your apps → Add app → Web, then copy `apiKey` and `appId` from the config it shows.
 * These values are public by design (they ship in the page); access is guarded by the rules.
 */
internal object FirebaseWebConfig {
    const val API_KEY = "AIzaSyCfvOjcdvSgRcHorBrxhOZTubXJWj2Xbbk"
    const val APP_ID = "1:853548872037:web:915f19965df6e71f0b718c"

    const val PROJECT_ID = "uggs-e27e4"
    const val AUTH_DOMAIN = "uggs-e27e4.firebaseapp.com"
    const val STORAGE_BUCKET = "uggs-e27e4.firebasestorage.app"
    const val MESSAGING_SENDER_ID = "853548872037"

    val isFilled: Boolean
        get() = !API_KEY.startsWith("PASTE") && !APP_ID.startsWith("PASTE")
}
