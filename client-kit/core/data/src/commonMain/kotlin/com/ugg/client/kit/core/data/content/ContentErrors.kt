package com.ugg.client.kit.core.data.content

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.kit.core.domain.exceptions.ContentUnavailableException
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException

/**
 * Runs a Firestore read and turns its failures into domain exceptions with a message the screen
 * can show as is.
 */
suspend fun <T> translatingContentErrors(block: suspend () -> T): T = try {
    block()
} catch (e: FirestoreException) {
    throw when (e.reason) {
        FirestoreException.Reason.Unavailable -> NoConnectionException(e)
        else -> ContentUnavailableException(cause = e)
    }
}
