package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.profile.data.mappers.sortedForDisplay
import com.ugg.client.kit.profile.data.mappers.toDomain
import com.ugg.client.kit.profile.data.mappers.toDto
import com.ugg.client.kit.profile.domain.entities.Address
import com.ugg.client.kit.profile.domain.exceptions.SignInRequiredException
import com.ugg.client.kit.profile.domain.useCases.AddressesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** `users/{uid}/addresses`, kept with exactly one default when any address exists. */
class SharedAddressesUseCase(
    private val auth: PhoneAuthService,
    private val users: UserService,
) : AddressesUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAddresses(): Flow<List<Address>> = auth.observeUser().flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else users.observeAddresses(user.uid).map { list -> list.map { it.toDomain() }.sortedForDisplay() }.translatingContentErrors()
    }

    override suspend fun address(id: String): Address? = translatingContentErrors {
        saved(uid()).firstOrNull { it.id == id }
    }

    override suspend fun save(address: Address): String {
        val uid = uid()
        return translatingContentErrors {
            val others = saved(uid).filter { it.id != address.id || address.id.isEmpty() }
            // The first address is the default; so is one marked by the user.
            val isDefault = address.isDefault || others.none { it.isDefault }
            val id = users.saveAddress(uid, address.copy(isDefault = isDefault).toDto())

            if (isDefault) {
                others.filter { it.isDefault }.forEach { users.saveAddress(uid, it.copy(isDefault = false).toDto()) }
            }
            id
        }
    }

    override suspend fun delete(id: String) {
        val uid = uid()
        translatingContentErrors {
            val all = saved(uid)
            users.deleteAddress(uid, id)

            val removed = all.firstOrNull { it.id == id }
            val heir = all.filter { it.id != id }.sortedForDisplay().firstOrNull()
            if (removed?.isDefault == true && heir != null) users.saveAddress(uid, heir.copy(isDefault = true).toDto())
        }
    }

    private suspend fun saved(uid: String): List<Address> = users.observeAddresses(uid).first().map { it.toDomain() }

    private fun uid() = auth.currentUser?.uid ?: throw SignInRequiredException()
}
