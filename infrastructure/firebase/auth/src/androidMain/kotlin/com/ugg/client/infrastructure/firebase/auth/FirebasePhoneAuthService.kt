package com.ugg.client.infrastructure.firebase.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun createPhoneAuthService(): PhoneAuthService = FirebasePhoneAuthService()

private const val CODE_TIMEOUT_SECONDS = 60L

internal class FirebasePhoneAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : PhoneAuthService {

    override fun observeUser(): Flow<AuthUser?> = callbackFlow {
        // Firebase calls the listener right away with the current state, then on every change.
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    override suspend fun sendCode(phone: String): PhoneVerification = suspendCancellableCoroutine { continuation ->
        val activity = PhoneAuthActivity.current
        if (activity == null) {
            continuation.resumeWithException(PhoneAuthException(PhoneAuthException.Reason.Unavailable))
            return@suspendCancellableCoroutine
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                if (continuation.isActive) continuation.resume(PhoneVerification.CodeSent(verificationId))
            }

            // Instant verification, or the SMS read automatically. It can also arrive after
            // onCodeSent — then the user is signed in silently and observeUser() reports it.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener

                    // Task.result throws on a failed task, so read it only after isSuccessful.
                    val user = if (task.isSuccessful) task.result?.user else null
                    if (user != null) {
                        continuation.resume(PhoneVerification.SignedIn(user.toAuthUser()))
                    } else {
                        continuation.resumeWithException(task.exception.toPhoneAuthException())
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                if (continuation.isActive) continuation.resumeWithException(exception.toPhoneAuthException())
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(CODE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build(),
        )
    }

    override suspend fun verify(verificationId: String, code: String): AuthUser {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val user = auth.signInWithCredential(credential).await().user

        return user?.toAuthUser() ?: throw PhoneAuthException(PhoneAuthException.Reason.Unknown)
    }

    override suspend fun signOut() = auth.signOut()

    override suspend fun deleteAccount() {
        val user = auth.currentUser ?: return
        user.delete().await()
    }
}

private fun FirebaseUser.toAuthUser() = AuthUser(uid = uid, phone = phoneNumber)

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) continuation.resume(task.result)
        else continuation.resumeWithException(task.exception.toPhoneAuthException())
    }
}

private fun Throwable?.toPhoneAuthException(): PhoneAuthException {
    val reason = when (this) {
        is FirebaseTooManyRequestsException -> PhoneAuthException.Reason.TooManyRequests
        is FirebaseNetworkException -> PhoneAuthException.Reason.Network
        is FirebaseAuthRecentLoginRequiredException -> PhoneAuthException.Reason.RecentLoginRequired
        is FirebaseAuthException -> when (errorCode) {
            "ERROR_INVALID_VERIFICATION_CODE" -> PhoneAuthException.Reason.InvalidCode
            "ERROR_INVALID_PHONE_NUMBER" -> PhoneAuthException.Reason.InvalidPhone
            "ERROR_SESSION_EXPIRED", "ERROR_CODE_EXPIRED" -> PhoneAuthException.Reason.CodeExpired
            "ERROR_TOO_MANY_REQUESTS", "ERROR_QUOTA_EXCEEDED" -> PhoneAuthException.Reason.TooManyRequests
            else -> PhoneAuthException.Reason.Unknown
        }
        is PhoneAuthException -> return this
        else -> PhoneAuthException.Reason.Unknown
    }

    return PhoneAuthException(reason, this)
}
