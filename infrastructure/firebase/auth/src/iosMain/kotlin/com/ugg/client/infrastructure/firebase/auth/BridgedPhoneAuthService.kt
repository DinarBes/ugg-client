package com.ugg.client.infrastructure.firebase.auth

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun createPhoneAuthService(): PhoneAuthService =
    IosPhoneAuth.bridge?.let(::BridgedPhoneAuthService) ?: UnavailablePhoneAuthService()

/** [PhoneAuthService] over the Firebase iOS SDK, through the Swift [PhoneAuthBridge]. */
internal class BridgedPhoneAuthService(private val bridge: PhoneAuthBridge) : PhoneAuthService {

    override fun observeUser(): Flow<AuthUser?> = callbackFlow {
        val registration = bridge.addUserListener { trySend(it) }
        awaitClose { registration.remove() }
    }

    override val currentUser: AuthUser?
        get() = bridge.currentUser

    override suspend fun sendCode(phone: String): PhoneVerification = suspendCancellableCoroutine { continuation ->
        bridge.sendCode(phone) { verificationId, error ->
            if (!continuation.isActive) return@sendCode
            when {
                error != null -> continuation.resumeWithException(error.toException())
                verificationId != null -> continuation.resume(PhoneVerification.CodeSent(verificationId))
                else -> continuation.resumeWithException(PhoneAuthException(PhoneAuthException.Reason.Unknown))
            }
        }
    }

    override suspend fun verify(verificationId: String, code: String): AuthUser = suspendCancellableCoroutine { continuation ->
        bridge.verify(verificationId, code) { user, error ->
            if (!continuation.isActive) return@verify
            when {
                error != null -> continuation.resumeWithException(error.toException())
                user != null -> continuation.resume(user)
                else -> continuation.resumeWithException(PhoneAuthException(PhoneAuthException.Reason.Unknown))
            }
        }
    }

    override suspend fun signOut() = bridge.signOut()

    override suspend fun deleteAccount() = suspendCancellableCoroutine { continuation ->
        bridge.deleteAccount { error ->
            if (!continuation.isActive) return@deleteAccount
            if (error != null) continuation.resumeWithException(error.toException()) else continuation.resume(Unit)
        }
    }
}

private fun PhoneAuthBridgeError.toException(): PhoneAuthException {
    val reason = PhoneAuthException.Reason.entries.firstOrNull { it.name == reason } ?: PhoneAuthException.Reason.Unknown
    return PhoneAuthException(reason, Exception(message))
}
