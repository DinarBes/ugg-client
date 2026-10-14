package com.ugg.client.kit.profile.domain.useCases

import com.ugg.client.kit.profile.domain.entities.Address
import kotlinx.coroutines.flow.Flow

interface AddressesUseCase {
    /** The default address first, then the rest by city and street. */
    fun observeAddresses(): Flow<List<Address>>

    suspend fun address(id: String): Address?

    /**
     * Adds ([Address.id] empty) or replaces the address and returns its id. The first address
     * becomes the default; making one the default takes the mark from the others.
     */
    suspend fun save(address: Address): String

    /** Deleting the default address passes the mark to another one, if any is left. */
    suspend fun delete(id: String)
}
