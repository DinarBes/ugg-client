package com.ugg.client.infrastructure.firebase.firestore

import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.FirestoreUserService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserServiceTest {

    @Test
    fun createdAtIsSetByTheServerOnlyOnTheFirstSave() = runTest {
        val store = FakeDocumentStore()
        val service = FirestoreUserService(store)

        assertNull(service.observeUser("u").first())

        service.saveUser("u", UserDto(name = "Анна", phone = "+79000000000"))
        assertEquals(FakeDocumentStore.NOW, service.observeUser("u").first()?.createdAt)

        // Pretend the first save happened long ago: a later save must not move the date.
        store.documents.update { it + ("users/u" to it.getValue("users/u") + ("createdAt" to 1L)) }
        service.saveUser("u", UserDto(name = "Аня", phone = "+79000000000"))

        val user = service.observeUser("u").first()
        assertEquals("Аня", user?.name)
        assertEquals(1L, user?.createdAt)
    }

    @Test
    fun newAddressGetsAnIdAndCanBeDeleted() = runTest {
        val service = FirestoreUserService(FakeDocumentStore())

        val id = service.saveAddress("u", AddressDto(city = "Иркутск", street = "ул. Ленина, 1"))
        assertEquals(listOf(id), service.observeAddresses("u").first().map { it.id })

        service.deleteAddress("u", id)
        assertEquals(emptyList(), service.observeAddresses("u").first())
    }
}
