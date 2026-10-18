package com.ugg.client.infrastructure.firebase.firestore

/**
 * Firestore as the iOS app hands it in: the Firebase iOS SDK is linked by the Swift side, which
 * implements this bridge and sets [IosFirestore.bridge] before the app starts.
 *
 * Documents cross as JSON, so neither side has to guess how numbers and booleans are boxed:
 * - a document is `{"id": "…", "data": {…}}`, a query result a JSON array of them, a missing
 *   document `null`;
 * - timestamps are read as epoch milliseconds, geo points travel as `{"latitude", "longitude"}`;
 * - in written data `{"__serverTimestamp": true}` stands for the server's time;
 * - a query is `{"collection", "filters": [{"field", "op": "==" | "in" | "not-in", "value"}],
 *   "orderBy", "descending", "limit"}`.
 *
 * Callbacks may come on any thread. Errors carry a [FirestoreException.Reason] name.
 */
interface FirestoreBridge {
    fun getDocument(path: String, completion: (json: String?, error: FirestoreBridgeError?) -> Unit)
    fun runQuery(queryJson: String, completion: (json: String?, error: FirestoreBridgeError?) -> Unit)
    fun observeDocument(path: String, onChange: (json: String?, error: FirestoreBridgeError?) -> Unit): FirestoreBridgeRegistration
    fun observeQuery(queryJson: String, onChange: (json: String?, error: FirestoreBridgeError?) -> Unit): FirestoreBridgeRegistration
    fun setDocument(path: String, dataJson: String, merge: Boolean, completion: (error: FirestoreBridgeError?) -> Unit)
    fun deleteDocument(path: String, completion: (error: FirestoreBridgeError?) -> Unit)
    fun generateId(collection: String): String

    /**
     * Runs [body] in a Firestore transaction, on the SDK's background queue and possibly several
     * times; a non-null result aborts it with that error. Then [completion] reports the outcome.
     */
    fun runTransaction(
        body: (transaction: FirestoreBridgeTransaction) -> FirestoreBridgeError?,
        completion: (error: FirestoreBridgeError?) -> Unit,
    )
}

/** Synchronous, as the SDK's transaction is. */
interface FirestoreBridgeTransaction {
    fun getDocument(path: String): FirestoreBridgeResult
    fun setDocument(path: String, dataJson: String)
}

interface FirestoreBridgeRegistration {
    fun remove()
}

/** [reason] is a [FirestoreException.Reason] name: «NotFound», «PermissionDenied», … */
class FirestoreBridgeError(val reason: String, val message: String)

class FirestoreBridgeResult(val json: String?, val error: FirestoreBridgeError?)

/** Where the iOS app puts its [FirestoreBridge]; without one every request fails with `Unavailable`. */
object IosFirestore {
    var bridge: FirestoreBridge? = null
}
