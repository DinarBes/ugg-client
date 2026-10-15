package com.ugg.client.infrastructure.firebase.auth

import com.ugg.client.infrastructure.firebase.auth.web.JsAuth
import com.ugg.client.infrastructure.firebase.auth.web.JsConfirmationResult
import com.ugg.client.infrastructure.firebase.auth.web.JsOutcome
import com.ugg.client.infrastructure.firebase.auth.web.JsUser
import com.ugg.client.infrastructure.firebase.auth.web.JsUserCredential
import com.ugg.client.infrastructure.firebase.auth.web.RecaptchaVerifier
import com.ugg.client.infrastructure.firebase.auth.web.deleteUser
import com.ugg.client.infrastructure.firebase.auth.web.freshRecaptchaElement
import com.ugg.client.infrastructure.firebase.auth.web.getAuth
import com.ugg.client.infrastructure.firebase.auth.web.invisibleRecaptcha
import com.ugg.client.infrastructure.firebase.auth.web.invokeFunction
import com.ugg.client.infrastructure.firebase.auth.web.onAuthStateChanged
import com.ugg.client.infrastructure.firebase.auth.web.settle
import com.ugg.client.infrastructure.firebase.auth.web.signInWithPhoneNumber
import com.ugg.client.infrastructure.firebase.auth.web.useRussian
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.js.Promise
import com.ugg.client.infrastructure.firebase.auth.web.signOut as jsSignOut

actual fun createPhoneAuthService(): PhoneAuthService = WebPhoneAuthService()

/**
 * Phone sign-in with the Firebase JS SDK. The browser has no SMS auto-read, so [sendCode] always
 * ends in `CodeSent`; an invisible reCAPTCHA guards the request (a challenge pops up only when
 * Google is not sure). The session survives a reload (IndexedDB), and [observeUser] first emits
 * once the SDK has restored it.
 */
internal class WebPhoneAuthService : PhoneAuthService {

    private val auth: JsAny by lazy { getAuth().also { useRussian(it) } }

    /** `confirm()` lives on the result of `signInWithPhoneNumber`, so it is kept per verification id. */
    private val confirmations = mutableMapOf<String, JsConfirmationResult>()

    override fun observeUser(): Flow<AuthUser?> = callbackFlow {
        val unsubscribe = onAuthStateChanged(auth) { user -> trySend(user?.unsafeCast<JsUser>()?.toAuthUser()) }
        awaitClose { invokeFunction(unsubscribe) }
    }

    override val currentUser: AuthUser?
        get() = auth.unsafeCast<JsAuth>().currentUser?.toAuthUser()

    override suspend fun sendCode(phone: String): PhoneVerification {
        val verifier = try {
            RecaptchaVerifier(auth, freshRecaptchaElement(), invisibleRecaptcha())
        } catch (e: Throwable) {
            throw PhoneAuthException(PhoneAuthException.Reason.Unavailable, e)
        }

        try {
            val confirmation = awaitValue(signInWithPhoneNumber(auth, phone, verifier))
                ?.unsafeCast<JsConfirmationResult>()
                ?: throw PhoneAuthException(PhoneAuthException.Reason.Unknown)

            confirmations[confirmation.verificationId] = confirmation
            return PhoneVerification.CodeSent(confirmation.verificationId)
        } finally {
            runCatching { verifier.clear() }
        }
    }

    override suspend fun verify(verificationId: String, code: String): AuthUser {
        // Gone after a page reload: the user has to ask for a new code.
        val confirmation = confirmations[verificationId]
            ?: throw PhoneAuthException(PhoneAuthException.Reason.CodeExpired)

        val credential = awaitValue(confirmation.confirm(code))?.unsafeCast<JsUserCredential>()
            ?: throw PhoneAuthException(PhoneAuthException.Reason.Unknown)

        confirmations.remove(verificationId)
        return credential.user.toAuthUser()
    }

    override suspend fun signOut() {
        awaitValue(jsSignOut(auth))
    }

    override suspend fun deleteAccount() {
        val user = auth.unsafeCast<JsAuth>().currentUser ?: return
        awaitValue(deleteUser(user))
    }
}

private fun JsUser.toAuthUser() = AuthUser(uid = uid, phone = phoneNumber)

private suspend fun awaitValue(promise: Promise<JsAny?>): JsAny? {
    val outcome = settle(promise).await<JsOutcome>()
    if (!outcome.ok) throw phoneAuthException(outcome.code, outcome.message)
    return outcome.value
}

/** Error codes of the JS SDK: https://firebase.google.com/docs/reference/js/auth#autherrorcodes */
private fun phoneAuthException(code: String, message: String): PhoneAuthException {
    val reason = when (code) {
        "auth/invalid-verification-code", "auth/missing-verification-code" -> PhoneAuthException.Reason.InvalidCode
        "auth/invalid-phone-number", "auth/missing-phone-number" -> PhoneAuthException.Reason.InvalidPhone
        "auth/code-expired", "auth/session-expired", "auth/invalid-verification-id" -> PhoneAuthException.Reason.CodeExpired
        "auth/too-many-requests", "auth/quota-exceeded" -> PhoneAuthException.Reason.TooManyRequests
        "auth/network-request-failed" -> PhoneAuthException.Reason.Network
        "auth/requires-recent-login" -> PhoneAuthException.Reason.RecentLoginRequired
        // The site is not in Authentication → Settings → Authorized domains, or phone sign-in is off.
        "auth/unauthorized-domain", "auth/operation-not-allowed", "auth/invalid-app-credential",
        "auth/invalid-api-key", "auth/app-not-authorized",
        -> PhoneAuthException.Reason.Unavailable
        else -> PhoneAuthException.Reason.Unknown
    }
    return PhoneAuthException(reason, Exception("$code: $message"))
}
