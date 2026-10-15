@file:JsModule("firebase/auth")

package com.ugg.client.infrastructure.firebase.auth.web

import kotlin.js.Promise

internal external fun getAuth(): JsAny

/** Called right away with the restored user (or null), then on every change; returns unsubscribe. */
internal external fun onAuthStateChanged(auth: JsAny, nextOrObserver: (JsAny?) -> Unit): JsAny

internal external fun signInWithPhoneNumber(auth: JsAny, phoneNumber: String, appVerifier: JsAny): Promise<JsAny?>

internal external fun signOut(auth: JsAny): Promise<JsAny?>

internal external fun deleteUser(user: JsAny): Promise<JsAny?>

internal external class RecaptchaVerifier(auth: JsAny, containerOrId: String, parameters: JsAny) : JsAny {
    fun clear()
}
