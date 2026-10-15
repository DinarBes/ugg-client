package com.ugg.client.infrastructure.firebase.firestore.internal

import kotlinx.coroutines.flow.Flow

/**
 * The few Firestore operations the services need, in plain Kotlin types, so that the services and
 * their tests stay in common code. Values are what Firestore stores, already converted:
 * strings, `Long`, `Double`, `Boolean`, `null`, lists, maps, [GeoPointValue], and timestamps as
 * epoch milliseconds (`Long`). Writing [ServerTimestamp] stores the server's time.
 *
 * Every failure is thrown (or emitted) as a FirestoreException.
 */
internal interface DocumentStore {
    suspend fun get(path: String): RawDocument?
    suspend fun query(query: RawQuery): List<RawDocument>
    fun observe(path: String): Flow<RawDocument?>
    fun observe(query: RawQuery): Flow<List<RawDocument>>

    /** Replaces the document, or with [merge] changes only the given fields. */
    suspend fun set(path: String, data: Map<String, Any?>, merge: Boolean = false)
    suspend fun delete(path: String)

    /** A fresh random id for a new document in [collection], without writing anything. */
    fun newId(collection: String): String

    /**
     * Runs [block] atomically; Firestore may run it several times when documents it read change
     * meanwhile, so it must not have side effects outside the [StoreTransaction].
     */
    suspend fun <T> transaction(block: suspend (StoreTransaction) -> T): T
}

internal interface StoreTransaction {
    /** Suspends on the web, where the JS SDK reads inside a transaction through a Promise. */
    suspend fun get(path: String): RawDocument?
    fun set(path: String, data: Map<String, Any?>)
}

internal data class RawDocument(
    val id: String,
    val data: Map<String, Any?>,
)

internal data class RawQuery(
    val collection: String,
    val filters: List<Filter> = emptyList(),
    val orderBy: String? = null,
    val descending: Boolean = false,
    val limit: Long? = null,
) {
    sealed interface Filter {
        val field: String

        data class EqualTo(override val field: String, val value: Any?) : Filter
        data class In(override val field: String, val values: List<Any>) : Filter
        data class NotIn(override val field: String, val values: List<Any>) : Filter
    }
}

internal data class GeoPointValue(val latitude: Double, val longitude: Double)

/** Placeholder for «the server's time» in written data. */
internal data object ServerTimestamp
