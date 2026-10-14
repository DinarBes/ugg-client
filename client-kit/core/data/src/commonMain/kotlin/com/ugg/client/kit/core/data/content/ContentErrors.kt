package com.ugg.client.kit.core.data.content

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.kit.core.domain.exceptions.ContentUnavailableException
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Runs a Firestore read and turns its failures into domain exceptions with a message the screen
 * can show as is.
 */
suspend fun <T> translatingContentErrors(block: suspend () -> T): T = try {
    block()
} catch (e: FirestoreException) {
    throw e.toDomain()
}

/** The same translation for a live Firestore stream. */
fun <T> Flow<T>.translatingContentErrors(): Flow<T> = flow {
    try {
        collect { emit(it) }
    } catch (e: FirestoreException) {
        throw e.toDomain()
    }
}

private fun FirestoreException.toDomain(): Exception = when (reason) {
    FirestoreException.Reason.Unavailable -> NoConnectionException(this)
    else -> ContentUnavailableException(cause = this)
}
