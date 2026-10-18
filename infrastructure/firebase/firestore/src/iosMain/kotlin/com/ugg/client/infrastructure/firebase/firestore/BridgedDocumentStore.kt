package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.FirestoreJson
import com.ugg.client.infrastructure.firebase.firestore.internal.RawDocument
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.ServerTimestamp
import com.ugg.client.infrastructure.firebase.firestore.internal.StoreTransaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal actual fun createDocumentStore(): DocumentStore =
    IosFirestore.bridge?.let(::BridgedDocumentStore) ?: UnavailableDocumentStore()

private const val SERVER_TIMESTAMP = "__serverTimestamp"

/** [DocumentStore] over the Firebase iOS SDK, through the Swift [FirestoreBridge]. */
internal class BridgedDocumentStore(private val bridge: FirestoreBridge) : DocumentStore {

    override suspend fun get(path: String): RawDocument? = suspendCancellableCoroutine { continuation ->
        bridge.getDocument(path) { json, error ->
            if (continuation.isActive) continuation.resumeWith(outcome(error) { decodeDocument(json) })
        }
    }

    override suspend fun query(query: RawQuery): List<RawDocument> = suspendCancellableCoroutine { continuation ->
        bridge.runQuery(query.toJson()) { json, error ->
            if (continuation.isActive) continuation.resumeWith(outcome(error) { decodeDocuments(json) })
        }
    }

    override fun observe(path: String): Flow<RawDocument?> = callbackFlow {
        val registration = bridge.observeDocument(path) { json, error ->
            outcome(error) { decodeDocument(json) }
                .onSuccess { trySend(it) }
                .onFailure { close(it) }
        }
        awaitClose { registration.remove() }
    }

    override fun observe(query: RawQuery): Flow<List<RawDocument>> = callbackFlow {
        val registration = bridge.observeQuery(query.toJson()) { json, error ->
            outcome(error) { decodeDocuments(json) }
                .onSuccess { trySend(it) }
                .onFailure { close(it) }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun set(path: String, data: Map<String, Any?>, merge: Boolean) = suspendCancellableCoroutine { continuation ->
        bridge.setDocument(path, encode(data), merge) { error ->
            if (continuation.isActive) continuation.resumeWith(outcome(error) { })
        }
    }

    override suspend fun delete(path: String) = suspendCancellableCoroutine { continuation ->
        bridge.deleteDocument(path) { error ->
            if (continuation.isActive) continuation.resumeWith(outcome(error) { })
        }
    }

    override fun newId(collection: String): String = bridge.generateId(collection)

    override suspend fun <T> transaction(block: suspend (StoreTransaction) -> T): T = suspendCancellableCoroutine { continuation ->
        // The outcome of the last run of the body; the SDK may run it again when a read changed.
        var attempt: Result<T>? = null

        bridge.runTransaction(
            body = { transaction ->
                // The SDK runs the body on its own queue and expects it to finish synchronously;
                // the block never really suspends here (BridgedTransaction.get is blocking).
                val result = runCatching { runBlocking { block(BridgedTransaction(transaction)) } }
                attempt = result
                result.exceptionOrNull()?.let { FirestoreBridgeError(reasonOf(it).name, it.message.orEmpty()) }
            },
            completion = { error ->
                val result = when (error) {
                    null -> attempt ?: Result.failure(FirestoreException(FirestoreException.Reason.Unknown))
                    // The block's own exception rather than the abort it caused.
                    else -> attempt?.takeIf { it.isFailure } ?: Result.failure(error.toException())
                }
                if (continuation.isActive) continuation.resumeWith(result)
            },
        )
    }
}

private class BridgedTransaction(private val transaction: FirestoreBridgeTransaction) : StoreTransaction {
    override suspend fun get(path: String): RawDocument? {
        val result = transaction.getDocument(path)
        result.error?.let { throw it.toException() }
        return decodeDocument(result.json)
    }

    override fun set(path: String, data: Map<String, Any?>) = transaction.setDocument(path, encode(data))
}

// ───────────── Errors ─────────────

private fun <T> outcome(error: FirestoreBridgeError?, value: () -> T): Result<T> =
    if (error != null) Result.failure(error.toException()) else runCatching(value)

private fun FirestoreBridgeError.toException(): FirestoreException {
    val reason = FirestoreException.Reason.entries.firstOrNull { it.name == reason } ?: FirestoreException.Reason.Unknown
    return FirestoreException(reason, Exception(message))
}

private fun reasonOf(throwable: Throwable): FirestoreException.Reason =
    (throwable as? FirestoreException)?.reason ?: FirestoreException.Reason.Unknown

// ───────────── JSON ⇄ plain Kotlin values ─────────────

private fun decodeDocument(json: String?): RawDocument? = malformedOnFailure {
    when (val element = json?.let { Json.parseToJsonElement(it) }) {
        null, JsonNull -> null
        else -> element.jsonObject.toRawDocument()
    }
}

private fun decodeDocuments(json: String?): List<RawDocument> = malformedOnFailure {
    json?.let { Json.parseToJsonElement(it) }?.jsonArray?.map { it.jsonObject.toRawDocument() }.orEmpty()
}

private fun JsonObject.toRawDocument(): RawDocument {
    val id = getValue(FirestoreJson.ID).jsonPrimitive.content
    // Field by field: a whole document with only «latitude» and «longitude» is not a geo point.
    val data = (get("data") as? JsonObject)?.mapValues { (_, value) -> FirestoreJson.fromJson(value) }.orEmpty()
    return RawDocument(id = id, data = data)
}

private inline fun <T> malformedOnFailure(block: () -> T): T = try {
    block()
} catch (e: FirestoreException) {
    throw e
} catch (e: Exception) {
    throw FirestoreException(FirestoreException.Reason.Malformed, e)
}

private fun encode(data: Map<String, Any?>): String = FirestoreJson.toJson(data.forBridge()).toString()

private fun Any?.forBridge(): Any? = when (this) {
    ServerTimestamp -> mapOf(SERVER_TIMESTAMP to true)
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value.forBridge() }
    is List<*> -> map { it.forBridge() }
    else -> this
}

private fun RawQuery.toJson(): String {
    val fields = buildMap<String, JsonElement> {
        put("collection", JsonPrimitive(collection))
        put(
            "filters",
            JsonArray(
                filters.map { filter ->
                    val (op, value) = when (filter) {
                        is RawQuery.Filter.EqualTo -> "==" to filter.value
                        is RawQuery.Filter.In -> "in" to filter.values
                        is RawQuery.Filter.NotIn -> "not-in" to filter.values
                    }
                    JsonObject(
                        mapOf(
                            "field" to JsonPrimitive(filter.field),
                            "op" to JsonPrimitive(op),
                            "value" to FirestoreJson.toJson(value.forBridge()),
                        ),
                    )
                },
            ),
        )
        orderBy?.let { put("orderBy", JsonPrimitive(it)) }
        put("descending", JsonPrimitive(descending))
        limit?.let { put("limit", JsonPrimitive(it)) }
    }
    return JsonObject(fields).toString()
}
