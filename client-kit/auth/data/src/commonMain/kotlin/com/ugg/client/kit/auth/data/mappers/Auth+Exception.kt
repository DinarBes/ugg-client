package com.ugg.client.kit.auth.data.mappers

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthException
import com.ugg.client.kit.auth.domain.exceptions.CodeExpiredException
import com.ugg.client.kit.auth.domain.exceptions.InvalidCodeException
import com.ugg.client.kit.auth.domain.exceptions.InvalidPhoneException
import com.ugg.client.kit.auth.domain.exceptions.SignInUnavailableException
import com.ugg.client.kit.auth.domain.exceptions.TooManyAttemptsException

/**
 * Sign-in failures in domain terms. Network and unknown errors go through as they are — the
 * screen shows them, nobody branches on them.
 */
internal fun PhoneAuthException.toDomain(): Exception = when (reason) {
    PhoneAuthException.Reason.InvalidPhone -> InvalidPhoneException()
    PhoneAuthException.Reason.InvalidCode -> InvalidCodeException()
    PhoneAuthException.Reason.CodeExpired -> CodeExpiredException()
    PhoneAuthException.Reason.TooManyRequests -> TooManyAttemptsException()
    PhoneAuthException.Reason.Unavailable -> SignInUnavailableException()
    PhoneAuthException.Reason.Network -> Exception("Нет соединения с интернетом", this)
    PhoneAuthException.Reason.RecentLoginRequired -> Exception("Войдите заново и повторите", this)
    PhoneAuthException.Reason.Unknown -> Exception("Не получилось войти. Попробуйте ещё раз", this)
}

internal inline fun <T> translatingAuthErrors(block: () -> T): T = try {
    block()
} catch (e: PhoneAuthException) {
    throw e.toDomain()
}
