package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.FirestoreJson
import com.ugg.client.infrastructure.firebase.firestore.internal.GeoPointValue
import com.ugg.client.infrastructure.firebase.firestore.internal.RawDocument
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.ServerTimestamp
import com.ugg.client.infrastructure.firebase.firestore.internal.StoreTransaction
import com.ugg.client.infrastructure.firebase.firestore.web.GeoPoint
import com.ugg.client.infrastructure.firebase.firestore.web.JsDocumentSnapshot
import com.ugg.client.infrastructure.firebase.firestore.web.JsOutcome
import com.ugg.client.infrastructure.firebase.firestore.web.JsQuerySnapshot
import com.ugg.client.infrastructure.firebase.firestore.web.JsTransaction
import com.ugg.client.infrastructure.firebase.firestore.web.collection as jsCollection
import com.ugg.client.infrastructure.firebase.firestore.web.deleteDoc
import com.ugg.client.infrastructure.firebase.firestore.web.doc
import com.ugg.client.infrastructure.firebase.firestore.web.errorCode
import com.ugg.client.infrastructure.firebase.firestore.web.errorMessage
import com.ugg.client.infrastructure.firebase.firestore.web.getDoc
import com.ugg.client.infrastructure.firebase.firestore.web.getDocs
import com.ugg.client.infrastructure.firebase.firestore.web.getFirestore
import com.ugg.client.infrastructure.firebase.firestore.web.invokeFunction
import com.ugg.client.infrastructure.firebase.firestore.web.limit as jsLimit
import com.ugg.client.infrastructure.firebase.firestore.web.mergeOptions
import com.ugg.client.infrastructure.firebase.firestore.web.onSnapshot
import com.ugg.client.infrastructure.firebase.firestore.web.orderBy as jsOrderBy
import com.ugg.client.infrastructure.firebase.firestore.web.parseForFirestore
import com.ugg.client.infrastructure.firebase.firestore.web.query as jsQuery
import com.ugg.client.infrastructure.firebase.firestore.web.runTransaction
import com.ugg.client.infrastructure.firebase.firestore.web.serverTimestamp
import com.ugg.client.infrastructure.firebase.firestore.web.setDoc
import com.ugg.client.infrastructure.firebase.firestore.web.settle
import com.ugg.client.infrastructure.firebase.firestore.web.snapshotJson
import com.ugg.client.infrastructure.firebase.firestore.web.where as jsWhere
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.promise
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.js.Promise
import kotlin.random.Random

internal actual fun createDocumentStore(): DocumentStore = WebDocumentStore()

/**
 * [DocumentStore] on the Firebase JS SDK. `initializeFirebaseWeb` (firebase-auth module) must run
 * before the first request; the Firestore instance is taken lazily for that reason.
 *
 * Data crosses the JS boundary as JSON with small markers for timestamps, GeoPoints and the
 * server-timestamp sentinel, so the conversion is the same as on Android: timestamps are epoch
 * milliseconds, GeoPoints are [GeoPointValue].
 */
internal class WebDocumentStore : DocumentStore {

    private val firestore: JsAny by lazy {
        try {
            getFirestore()
        } catch (e: Throwable) {
            throw FirestoreException(FirestoreException.Reason.Unavailable, e)
        }
    }

    override suspend fun get(path: String): RawDocument? =
        awaitValue(getDoc(doc(firestore, path)))?.toRawDocument()

    override suspend fun query(query: RawQuery): List<RawDocument> =
        awaitValue(getDocs(query.toJsQuery()))?.toRawDocuments().orEmpty()

    override fun observe(path: String): Flow<RawDocument?> = callbackFlow {
        val unsubscribe = onSnapshot(
            doc(firestore, path),
            { snapshot -> trySend(snapshot.toRawDocument()) },
            { error -> close(firestoreException(errorCode(error), errorMessage(error))) },
        )
        awaitClose { invokeFunction(unsubscribe) }
    }

    override fun observe(query: RawQuery): Flow<List<RawDocument>> = callbackFlow {
        val unsubscribe = onSnapshot(
            query.toJsQuery(),
            { snapshot -> trySend(snapshot.toRawDocuments()) },
            { error -> close(firestoreException(errorCode(error), errorMessage(error))) },
        )
        awaitClose { invokeFunction(unsubscribe) }
    }

    override suspend fun set(path: String, data: Map<String, Any?>, merge: Boolean) {
        awaitValue(setDoc(doc(firestore, path), data.toJs(), mergeOptions(merge)))
    }

    override suspend fun delete(path: String) {
        awaitValue(deleteDoc(doc(firestore, path)))
    }

    /** Same alphabet and length as the SDK's own auto ids. */
    override fun newId(collection: String): String =
        (1..AUTO_ID_LENGTH).map { AUTO_ID_ALPHABET[Random.nextInt(AUTO_ID_ALPHABET.length)] }.joinToString("")

    override suspend fun <T> transaction(block: suspend (StoreTransaction) -> T): T {
        // The SDK calls the update function (possibly several times) and waits for its Promise.
        // The block runs in a supervisor child, so a failed attempt rejects that Promise — which
        // aborts the transaction — instead of cancelling the caller.
        val supervisor = SupervisorJob(currentCoroutineContext()[Job])
        val scope = CoroutineScope(currentCoroutineContext() + supervisor)
        var last: Result<T>? = null

        try {
            val outcome = settle(
                runTransaction(firestore) { transaction ->
                    scope.promise<JsAny?> {
                        val attempt = runCatching { block(WebTransaction(transaction.unsafeCast<JsTransaction>())) }
                        last = attempt
                        attempt.getOrThrow()
                        null
                    }
                },
            ).await<JsOutcome>()

            val result = last
            if (!outcome.ok) {
                // Our own exception (e.g. FirestoreException(NotFound)) wins over the SDK's wrapper.
                result?.exceptionOrNull()?.let { throw it }
                throw firestoreException(outcome.code, outcome.message)
            }
            return result?.getOrThrow() ?: throw FirestoreException(FirestoreException.Reason.Unknown)
        } finally {
            supervisor.cancel()
        }
    }

    private inner class WebTransaction(private val transaction: JsTransaction) : StoreTransaction {
        override suspend fun get(path: String): RawDocument? =
            awaitValue(transaction.get(doc(firestore, path)))?.toRawDocument()

        override fun set(path: String, data: Map<String, Any?>) {
            transaction.set(doc(firestore, path), data.toJs())
        }
    }

    private fun RawQuery.toJsQuery(): JsAny {
        var result: JsAny = jsCollection(firestore, collection)
        filters.forEach { filter ->
            val constraint = when (filter) {
                is RawQuery.Filter.EqualTo -> jsWhere(filter.field, "==", filter.value.toJsValue())
                is RawQuery.Filter.In -> jsWhere(filter.field, "in", filter.values.toJsValue())
                is RawQuery.Filter.NotIn -> jsWhere(filter.field, "not-in", filter.values.toJsValue())
            }
            result = jsQuery(result, constraint)
        }
        orderBy?.let { result = jsQuery(result, jsOrderBy(it, if (descending) "desc" else "asc")) }
        limit?.let { result = jsQuery(result, jsLimit(it.toInt())) }
        return result
    }

    private companion object {
        const val AUTO_ID_LENGTH = 20
        const val AUTO_ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}

// ───────────── Promises and errors ─────────────

private suspend fun awaitValue(promise: Promise<JsAny?>): JsAny? {
    val outcome = settle(promise).await<JsOutcome>()
    if (!outcome.ok) throw firestoreException(outcome.code, outcome.message)
    return outcome.value
}

/** Error codes of the JS SDK: https://firebase.google.com/docs/reference/js/firestore_.firestoreerrorcode */
private fun firestoreException(code: String, message: String): FirestoreException {
    val reason = when (code) {
        "not-found" -> FirestoreException.Reason.NotFound
        "permission-denied" -> FirestoreException.Reason.PermissionDenied
        "unauthenticated" -> FirestoreException.Reason.Unauthenticated
        "unavailable", "deadline-exceeded" -> FirestoreException.Reason.Unavailable
        else -> FirestoreException.Reason.Unknown
    }
    return FirestoreException(reason, Exception("$code: $message"))
}

// ───────────── JS values ⇄ plain Kotlin values ─────────────

private const val MILLIS = "__ms"
private const val GEO = "__geo"
private const val SERVER_TIMESTAMP = "__serverTimestamp"

private fun JsAny.toRawDocument(): RawDocument? {
    val json = snapshotJson(this) ?: return null
    @Suppress("UNCHECKED_CAST")
    val data = Json.parseToJsonElement(json).toRawValue() as? Map<String, Any?> ?: emptyMap()
    return RawDocument(id = unsafeCast<JsDocumentSnapshot>().id, data = data)
}

private fun JsAny.toRawDocuments(): List<RawDocument> {
    val docs = unsafeCast<JsQuerySnapshot>().docs
    return (0 until docs.length).mapNotNull { index -> docs[index]?.toRawDocument() }
}

private fun JsonElement.toRawValue(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> if (isString) content else booleanOrNull ?: longOrNull ?: doubleOrNull
    is JsonArray -> map { it.toRawValue() }
    is JsonObject -> {
        val millis = this[MILLIS] as? JsonPrimitive
        val geo = this[GEO] as? JsonArray
        when {
            // toMillis() may carry a fraction of a millisecond.
            size == 1 && millis != null -> millis.doubleOrNull?.toLong()
            size == 1 && geo != null && geo.size == 2 -> GeoPointValue(
                latitude = (geo[0] as JsonPrimitive).doubleOrNull ?: 0.0,
                longitude = (geo[1] as JsonPrimitive).doubleOrNull ?: 0.0,
            )
            else -> mapValues { (_, value) -> value.toRawValue() }
        }
    }
}

private fun Map<String, Any?>.toJs(): JsAny = toJsValue() ?: error("a map never encodes to null")

private fun Any?.toJsValue(): JsAny? = parseForFirestore(
    json = toMarkedJson().toString(),
    serverTimestamp = serverTimestamp(),
    geoPoint = { latitude, longitude -> GeoPoint(latitude, longitude) },
)

private fun Any?.toMarkedJson(): JsonElement = when (this) {
    ServerTimestamp -> JsonObject(mapOf(SERVER_TIMESTAMP to JsonPrimitive(true)))
    is GeoPointValue -> JsonObject(mapOf(GEO to JsonArray(listOf(JsonPrimitive(latitude), JsonPrimitive(longitude)))))
    is Map<*, *> -> JsonObject(entries.associate { (key, value) -> key.toString() to value.toMarkedJson() })
    is List<*> -> JsonArray(map { it.toMarkedJson() })
    else -> FirestoreJson.toJson(this)
}
