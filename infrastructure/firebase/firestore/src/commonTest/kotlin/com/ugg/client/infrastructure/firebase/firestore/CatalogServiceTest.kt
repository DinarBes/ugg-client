package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.internal.GeoPointValue
import com.ugg.client.infrastructure.firebase.firestore.services.FirestoreCatalogService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CatalogServiceTest {

    @Test
    fun productIsReadWithItsIdAndNestedLists() = runTest {
        val service = FirestoreCatalogService(FakeDocumentStore(mapOf("products/classic" to PRODUCT)))

        val product = service.product()

        assertEquals("classic", product.id)
        assertEquals(14900, product.price)
        assertEquals(5, product.leadTimeDays.min)
        assertEquals("#B98A5E", product.shades.single().color)
        assertEquals(false, product.sizes.single().available)
        assertNull(product.customSizeHint)
    }

    @Test
    fun missingDocumentIsNotFound() = runTest {
        val service = FirestoreCatalogService(FakeDocumentStore())

        val error = assertFailsWith<FirestoreException> { service.home() }

        assertEquals(FirestoreException.Reason.NotFound, error.reason)
    }

    @Test
    fun documentOfWrongShapeIsMalformed() = runTest {
        val service = FirestoreCatalogService(FakeDocumentStore(mapOf("products/classic" to mapOf("price" to "дорого"))))

        val error = assertFailsWith<FirestoreException> { service.product() }

        assertEquals(FirestoreException.Reason.Malformed, error.reason)
    }

    @Test
    fun storesComeInTheirOrderWithLocationAndDaysOff() = runTest {
        val service = FirestoreCatalogService(
            FakeDocumentStore(
                mapOf(
                    "stores/listvyanka" to mapOf("order" to 1L, "city" to "Листвянка", "schedule" to mapOf("mon" to null)),
                    "stores/irkutsk" to mapOf(
                        "order" to 0L,
                        "city" to "Иркутск",
                        "location" to GeoPointValue(52.287, 104.281),
                        "schedule" to mapOf("mon" to mapOf("open" to "10:00", "close" to "20:00")),
                    ),
                ),
            ),
        )

        val stores = service.stores()

        assertEquals(listOf("irkutsk", "listvyanka"), stores.map { it.id })
        assertEquals(52.287, stores[0].location.latitude)
        assertEquals("10:00", stores[0].schedule.mon?.open)
        assertNull(stores[1].schedule.mon)
    }

    private companion object {
        val PRODUCT = mapOf(
            "title" to "Классика",
            "price" to 14900L,
            "leadTimeDays" to mapOf("min" to 5L, "max" to 7L),
            "shades" to listOf(mapOf("id" to "chestnut", "name" to "Каштан", "color" to "#B98A5E", "inStock" to true)),
            "sizes" to listOf(mapOf("label" to "44", "available" to false)),
            "customSizeHint" to null,
            "somethingNew" to "ignored",
        )
    }
}
