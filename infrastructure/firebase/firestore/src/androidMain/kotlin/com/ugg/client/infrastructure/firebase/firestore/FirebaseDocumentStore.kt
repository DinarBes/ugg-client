package com.ugg.client.infrastructure.firebase.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.GeoPointValue
import com.ugg.client.infrastructure.firebase.firestore.internal.RawDocument
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.ServerTimestamp
import com.ugg.client.infrastructure.firebase.firestore.internal.StoreTransaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual fun createDocumentStore(): DocumentStore = FirebaseDocumentStore()

/**
 * [DocumentStore] on the Firestore Android SDK. The SDK keeps an offline cache, so reads made
 * once keep working without network; listeners fire again when the data changes.
 */
internal class FirebaseDocumentStore(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : DocumentStore {

    override suspend fun get(path: String): RawDocument? =
        firestore.document(path).get().await().toRawDocument()

    override suspend fun query(query: RawQuery): List<RawDocument> =
        query.toFirestoreQuery().get().await().documents.mapNotNull { it.toRawDocument() }

    override fun observe(path: String): Flow<RawDocument?> = callbackFlow {
        val registration = firestore.document(path).addSnapshotListener { snapshot, error ->
            if (error != null) close(error.toFirestoreException())
            else trySend(snapshot?.toRawDocument())
        }
        awaitClose { registration.remove() }
    }

    override fun observe(query: RawQuery): Flow<List<RawDocument>> = callbackFlow {
        val registration = query.toFirestoreQuery().addSnapshotListener { snapshot, error ->
            if (error != null) close(error.toFirestoreException())
            else trySend(snapshot?.documents?.mapNotNull { it.toRawDocument() }.orEmpty())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun set(path: String, data: Map<String, Any?>, merge: Boolean) {
        val reference = firestore.document(path)
        val values = data.toFirestore()
        (if (merge) reference.set(values, SetOptions.merge()) else reference.set(values)).await()
    }

    override suspend fun delete(path: String) {
        firestore.document(path).delete().await()
    }

    override fun newId(collection: String): String = firestore.collection(collection).document().id

    override suspend fun <T> transaction(block: (StoreTransaction) -> T): T =
        firestore.runTransaction { transaction ->
            block(object : StoreTransaction {
                override fun get(path: String) = transaction.get(firestore.document(path)).toRawDocument()
                override fun set(path: String, data: Map<String, Any?>) {
                    transaction.set(firestore.document(path), data.toFirestore())
                }
            })
        }.await()

    private fun RawQuery.toFirestoreQuery(): Query {
        var query: Query = firestore.collection(collection)
        filters.forEach { filter ->
            query = when (filter) {
                is RawQuery.Filter.EqualTo -> query.whereEqualTo(filter.field, filter.value)
                is RawQuery.Filter.In -> query.whereIn(filter.field, filter.values)
                is RawQuery.Filter.NotIn -> query.whereNotIn(filter.field, filter.values)
            }
        }
        orderBy?.let { query = query.orderBy(it, if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING) }
        limit?.let { query = query.limit(it) }
        return query
    }
}

// ───────────── Firestore values ⇄ plain Kotlin values ─────────────

private fun DocumentSnapshot.toRawDocument(): RawDocument? {
    // ESTIMATE: a server timestamp just written offline reads as the device's guess instead of null.
    val data = getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return null
    return RawDocument(id = id, data = data.mapValues { (_, value) -> value.fromFirestore() })
}

private fun Any?.fromFirestore(): Any? = when (this) {
    is Timestamp -> toDate().time
    is GeoPoint -> GeoPointValue(latitude, longitude)
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.fromFirestore() }
    is List<*> -> map { it.fromFirestore() }
    is Int -> toLong()
    is Float -> toDouble()
    else -> this
}

private fun Map<String, Any?>.toFirestore(): Map<String, Any?> = mapValues { (_, value) -> value.toFirestoreValue() }

private fun Any?.toFirestoreValue(): Any? = when (this) {
    ServerTimestamp -> FieldValue.serverTimestamp()
    is GeoPointValue -> GeoPoint(latitude, longitude)
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.toFirestoreValue() }
    is List<*> -> map { it.toFirestoreValue() }
    else -> this
}

// ───────────── Tasks and errors ─────────────

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) continuation.resume(task.result)
        else continuation.resumeWithException(task.exception.toFirestoreException())
    }
}

private fun Throwable?.toFirestoreException(): FirestoreException {
    // An exception thrown inside a transaction comes back as is, or wrapped by the SDK.
    generateSequence(this) { it.cause }.firstNotNullOfOrNull { it as? FirestoreException }?.let { return it }

    val reason = when (this) {
        is FirebaseNetworkException -> FirestoreException.Reason.Unavailable
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.NOT_FOUND -> FirestoreException.Reason.NotFound
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FirestoreException.Reason.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> FirestoreException.Reason.Unauthenticated
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            -> FirestoreException.Reason.Unavailable
            else -> FirestoreException.Reason.Unknown
        }
        else -> FirestoreException.Reason.Unknown
    }
    return FirestoreException(reason, this)
}
