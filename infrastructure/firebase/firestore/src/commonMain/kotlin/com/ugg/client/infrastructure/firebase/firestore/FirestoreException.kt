package com.ugg.client.infrastructure.firebase.firestore

/** Firestore failures translated into reasons the app can branch on, without Firebase types. */
class FirestoreException(
    val reason: Reason,
    cause: Throwable? = null,
) : Exception(reason.name, cause) {

    enum class Reason {
        /** The document is missing: the catalog was not seeded, or the id is wrong. */
        NotFound,
        /** Security rules refused the request. */
        PermissionDenied,
        /** The request needs a signed-in user. */
        Unauthenticated,
        /** No network and nothing in the offline cache, or Firestore is not available on this platform. */
        Unavailable,
        /** The document exists but does not match the expected shape. */
        Malformed,
        Unknown,
    }
}
