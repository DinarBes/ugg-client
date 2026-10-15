package com.ugg.client.infrastructure.firebase.auth.web

import kotlin.js.Promise

internal external interface JsAuth : JsAny {
    val currentUser: JsUser?
}

internal external interface JsUser : JsAny {
    val uid: String
    val phoneNumber: String?
}

internal external interface JsUserCredential : JsAny {
    val user: JsUser
}

internal external interface JsConfirmationResult : JsAny {
    val verificationId: String
    fun confirm(verificationCode: String): Promise<JsAny?>
}

/** A settled Promise: never rejects, so a JS error turns into a Kotlin exception we choose. */
internal external interface JsOutcome : JsAny {
    val ok: Boolean
    val value: JsAny?
    val code: String
    val message: String
}

internal fun settle(promise: Promise<JsAny?>): Promise<JsOutcome> = js(
    """promise.then(
        (v) => ({ ok: true, value: v === undefined ? null : v, code: '', message: '' }),
        (e) => ({ ok: false, value: null, code: String((e && e.code) || ''), message: String((e && e.message) || e) })
    )""",
)

internal fun invokeFunction(callback: JsAny): Unit = js("callback()")

internal fun invisibleRecaptcha(): JsAny = js("({ size: 'invisible' })")

internal fun useRussian(auth: JsAny): Unit = js("auth.languageCode = 'ru'")

/**
 * A fresh element for the reCAPTCHA widget inside `#recaptcha-container` (index.html), so a new
 * verifier never meets the leftovers of the previous one.
 */
internal fun freshRecaptchaElement(): String = js(
    """(() => {
        const element = document.createElement('div');
        element.id = 'recaptcha-' + Date.now();
        const host = document.getElementById('recaptcha-container');
        if (host) host.replaceChildren(element); else document.body.appendChild(element);
        return element.id;
    })()""",
)

internal fun firebaseOptions(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
): JsAny = js(
    """({ apiKey: apiKey, authDomain: authDomain, projectId: projectId, storageBucket: storageBucket,
        messagingSenderId: messagingSenderId, appId: appId })""",
)
