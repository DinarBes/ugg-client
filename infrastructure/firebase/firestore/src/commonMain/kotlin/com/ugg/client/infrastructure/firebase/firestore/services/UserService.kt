package com.ugg.client.infrastructure.firebase.firestore.services

import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.internal.DocumentStore
import com.ugg.client.infrastructure.firebase.firestore.internal.FirestoreJson
import com.ugg.client.infrastructure.firebase.firestore.internal.RawQuery
import com.ugg.client.infrastructure.firebase.firestore.internal.ServerTimestamp
import com.ugg.client.infrastructure.firebase.firestore.internal.decode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** `users/{uid}` and its addresses. Only the signed-in user can read and write their own. */
interface UserService {
    /** `null` until the profile is saved for the first time. */
    fun observeUser(uid: String): Flow<UserDto?>

    /**
     * One read, from the server when online (the offline cache otherwise) — for decisions such
     * as «is this a new account», where a not-yet-cached document must not look missing.
     */
    suspend fun getUser(uid: String): UserDto?

    /**
     * Saves the whole profile (fields left at their defaults are written too, so pass the user
     * read from [observeUser] with the changes applied). `createdAt` is set once by the server.
     */
    suspend fun saveUser(uid: String, user: UserDto)

    fun observeAddresses(uid: String): Flow<List<AddressDto>>

    /** Adds the address when its id is empty, replaces it otherwise. Returns the id. */
    suspend fun saveAddress(uid: String, address: AddressDto): String

    suspend fun deleteAddress(uid: String, addressId: String)
}

internal class FirestoreUserService(
    private val store: DocumentStore,
) : UserService {

    override fun observeUser(uid: String): Flow<UserDto?> =
        store.observe(userPath(uid)).map { it?.decode(UserDto.serializer()) }

    override suspend fun getUser(uid: String): UserDto? = store.get(userPath(uid))?.decode(UserDto.serializer())

    override suspend fun saveUser(uid: String, user: UserDto) {
        val existing = store.get(userPath(uid))
        val data = FirestoreJson.encode(UserDto.serializer(), user) - "createdAt"
        val withCreatedAt = if (existing == null) data + ("createdAt" to ServerTimestamp) else data
        store.set(userPath(uid), withCreatedAt, merge = true)
    }

    override fun observeAddresses(uid: String): Flow<List<AddressDto>> =
        store.observe(RawQuery(collection = addressesPath(uid)))
            .map { documents -> documents.map { it.decode(AddressDto.serializer()) } }

    override suspend fun saveAddress(uid: String, address: AddressDto): String {
        val id = address.id.ifEmpty { store.newId(addressesPath(uid)) }
        store.set("${addressesPath(uid)}/$id", FirestoreJson.encode(AddressDto.serializer(), address))
        return id
    }

    override suspend fun deleteAddress(uid: String, addressId: String) =
        store.delete("${addressesPath(uid)}/$addressId")

    private fun userPath(uid: String) = "users/$uid"
    private fun addressesPath(uid: String) = "users/$uid/addresses"
}
