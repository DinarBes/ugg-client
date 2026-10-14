package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.kit.profile.data.mappers.sortedForDisplay
import com.ugg.client.kit.profile.domain.entities.Address
import com.ugg.client.kit.profile.domain.useCases.AddressesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Addresses in memory, with the same default rules as the real one. */
class MockAddressesUseCase : AddressesUseCase {
    private val addresses = MutableStateFlow(
        listOf(Address(id = "home", city = "Иркутск", street = "ул. Ленина, 12", flat = "5", isDefault = true)),
    )
    private var nextId = 0

    override fun observeAddresses(): Flow<List<Address>> = addresses.map { it.sortedForDisplay() }

    override suspend fun address(id: String) = addresses.value.firstOrNull { it.id == id }

    override suspend fun save(address: Address): String {
        val id = address.id.ifEmpty { "mock-${++nextId}" }
        addresses.update { list ->
            val others = list.filter { it.id != id }
            val isDefault = address.isDefault || others.none { it.isDefault }
            others.map { if (isDefault) it.copy(isDefault = false) else it } + address.copy(id = id, isDefault = isDefault)
        }
        return id
    }

    override suspend fun delete(id: String) = addresses.update { list ->
        val rest = list.filter { it.id != id }
        if (rest.isNotEmpty() && rest.none { it.isDefault }) {
            val heir = rest.sortedForDisplay().first()
            rest.map { if (it.id == heir.id) it.copy(isDefault = true) else it }
        } else {
            rest
        }
    }
}
