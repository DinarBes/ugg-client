package com.ugg.client.kit.care.data.useCases

import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.care.data.mappers.toDomain
import com.ugg.client.kit.care.domain.entities.Care
import com.ugg.client.kit.care.domain.useCases.CareUseCase
import com.ugg.client.kit.core.data.content.translatingContentErrors

/** «Уход за уггами» from Firestore, `content/care`. */
class SharedCareUseCase(
    private val catalog: CatalogService,
) : CareUseCase {

    override suspend fun fetch(): Care = translatingContentErrors { catalog.care().toDomain() }
}
