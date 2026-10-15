package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.RawDocument
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.ServerTimestamp
import com.ugg.client.infrastructure.firebase.firestore.internal.StoreTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory Firestore: documents by full path, server time is always [NOW]. */
internal class FakeDocumentStore(initial: Map<String, Map<String, Any?>> = emptyMap()) : DocumentStore {
    val documents = MutableStateFlow(initial)
    private var ids = 0

    override suspend fun get(path: String) = read(path)
    override suspend fun query(query: RawQuery) = select(query, documents.value)
    override fun observe(path: String): Flow<RawDocument?> = documents.map { read(path, it) }
    override fun observe(query: RawQuery): Flow<List<RawDocument>> = documents.map { select(query, it) }

    override suspend fun set(path: String, data: Map<String, Any?>, merge: Boolean) = documents.update { all ->
        val base = if (merge) all[path].orEmpty() else emptyMap()
        all + (path to base + data.resolved())
    }

    override suspend fun delete(path: String) = documents.update { it - path }

    override fun newId(collection: String) = "id-${++ids}"

    override suspend fun <T> transaction(block: suspend (StoreTransaction) -> T): T {
        val writes = mutableMapOf<String, Map<String, Any?>>()
        val result = block(object : StoreTransaction {
            override suspend fun get(path: String) = read(path)
            override fun set(path: String, data: Map<String, Any?>) {
                writes[path] = data.resolved()
            }
        })
        documents.update { it + writes }
        return result
    }

    private fun read(path: String, all: Map<String, Map<String, Any?>> = documents.value) =
        all[path]?.let { RawDocument(id = path.substringAfterLast('/'), data = it) }

    private fun select(query: RawQuery, all: Map<String, Map<String, Any?>>): List<RawDocument> {
        val prefix = query.collection + "/"
        var result = all
            .filterKeys { it.startsWith(prefix) && '/' !in it.removePrefix(prefix) }
            .map { (path, data) -> RawDocument(path.substringAfterLast('/'), data) }
            .filter { document -> query.filters.all { it.matches(document.data) } }
        query.orderBy?.let { field ->
            result = result.sortedWith(compareBy { it.data[field] as? Comparable<*> })
            if (query.descending) result = result.reversed()
        }
        return query.limit?.let { result.take(it.toInt()) } ?: result
    }

    private fun RawQuery.Filter.matches(data: Map<String, Any?>) = when (this) {
        is RawQuery.Filter.EqualTo -> data[field] == value
        is RawQuery.Filter.In -> data[field] in values
        is RawQuery.Filter.NotIn -> data[field] !in values
    }

    private fun Map<String, Any?>.resolved() = mapValues { (_, value) -> if (value == ServerTimestamp) NOW else value }

    companion object {
        const val NOW = 1_760_000_000_000L
    }
}
