package com.ugg.client.infrastructure.firebase.auth

import com.ugg.client.infrastructure.firebase.auth.web.firebaseOptions
import com.ugg.client.infrastructure.firebase.auth.web.initializeApp

/**
 * Starts the Firebase JS SDK. The web app calls it once in `main()`, before `TemplateApp` —
 * Auth and Firestore both take the default app it creates. The values come from Firebase console
 * → Project settings → Your apps → Web app.
 */
fun initializeFirebaseWeb(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
) {
    initializeApp(firebaseOptions(apiKey, authDomain, projectId, storageBucket, messagingSenderId, appId))
}
