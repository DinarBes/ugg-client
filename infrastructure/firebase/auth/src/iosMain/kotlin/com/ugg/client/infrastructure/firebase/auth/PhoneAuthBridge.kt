package com.ugg.client.infrastructure.firebase.auth

/**
 * Firebase phone sign-in as the iOS app hands it in: the Firebase iOS SDK is linked by the Swift
 * side, which implements this bridge and sets [IosPhoneAuth.bridge] before the app starts.
 *
 * Callbacks may come on any thread. Errors carry a [PhoneAuthException.Reason] name.
 */
interface PhoneAuthBridge {
    /** `null` — a guest. */
    val currentUser: AuthUser?

    /** Calls [onChange] right away with the current user, then on every sign-in and sign-out. */
    fun addUserListener(onChange: (user: AuthUser?) -> Unit): PhoneAuthBridgeRegistration

    /** Sends the SMS (verifying the app with a silent push or reCAPTCHA first); gives the verification id. */
    fun sendCode(phone: String, completion: (verificationId: String?, error: PhoneAuthBridgeError?) -> Unit)

    fun verify(verificationId: String, code: String, completion: (user: AuthUser?, error: PhoneAuthBridgeError?) -> Unit)

    fun signOut()

    fun deleteAccount(completion: (error: PhoneAuthBridgeError?) -> Unit)
}

interface PhoneAuthBridgeRegistration {
    fun remove()
}

/** [reason] is a [PhoneAuthException.Reason] name: «InvalidCode», «Network», … */
class PhoneAuthBridgeError(val reason: String, val message: String)

/** Where the iOS app puts its [PhoneAuthBridge]; without one everyone stays a guest. */
object IosPhoneAuth {
    var bridge: PhoneAuthBridge? = null
}
