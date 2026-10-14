package com.ugg.client.infrastructure.firebase.firestore.services

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.CareContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.decode
import kotlinx.serialization.KSerializer

/**
 * The read-only part of the database: the product, the stores and the screen texts. Readable
 * without signing in. A missing document throws [FirestoreException] with `NotFound`.
 */
interface CatalogService {
    suspend fun product(id: String = DEFAULT_PRODUCT_ID): ProductDto

    /** Sorted by their `order` field. */
    suspend fun stores(): List<StoreDto>

    suspend fun home(): HomeContentDto
    suspend fun story(): StoryContentDto
    suspend fun onboarding(): OnboardingContentDto
    suspend fun checkout(): CheckoutContentDto
    suspend fun care(): CareContentDto

    companion object {
        /** The shop sells one model. */
        const val DEFAULT_PRODUCT_ID = "classic"
    }
}

internal class FirestoreCatalogService(
    private val store: DocumentStore,
) : CatalogService {

    override suspend fun product(id: String) = document("products/$id", ProductDto.serializer())

    override suspend fun stores() = store
        .query(RawQuery(collection = "stores", orderBy = "order"))
        .map { it.decode(StoreDto.serializer()) }

    override suspend fun home() = document("content/home", HomeContentDto.serializer())
    override suspend fun story() = document("content/story", StoryContentDto.serializer())
    override suspend fun onboarding() = document("content/onboarding", OnboardingContentDto.serializer())
    override suspend fun checkout() = document("content/checkout", CheckoutContentDto.serializer())
    override suspend fun care() = document("content/care", CareContentDto.serializer())

    private suspend fun <T> document(path: String, serializer: KSerializer<T>): T =
        (store.get(path) ?: throw FirestoreException(FirestoreException.Reason.NotFound))
            .decode(serializer)
}
