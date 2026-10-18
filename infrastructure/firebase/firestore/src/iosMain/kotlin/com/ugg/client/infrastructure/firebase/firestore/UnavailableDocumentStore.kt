package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.RawDocument
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.StoreTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Until the iOS app sets [IosFirestore.bridge] (or in a host without Firebase): every request
 * fails with `Unavailable`, and the app shows its error state.
 */
internal class UnavailableDocumentStore : DocumentStore {
    override suspend fun get(path: String): RawDocument? = unavailable()
    override suspend fun query(query: RawQuery): List<RawDocument> = unavailable()
    override fun observe(path: String): Flow<RawDocument?> = flow { unavailable() }
    override fun observe(query: RawQuery): Flow<List<RawDocument>> = flow { unavailable() }
    override suspend fun set(path: String, data: Map<String, Any?>, merge: Boolean) = unavailable()
    override suspend fun delete(path: String) = unavailable()
    override fun newId(collection: String): String = unavailable()
    override suspend fun <T> transaction(block: suspend (StoreTransaction) -> T): T = unavailable()

    private fun unavailable(): Nothing = throw FirestoreException(FirestoreException.Reason.Unavailable)
}
