package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.kit.profile.domain.entities.Account
import com.ugg.client.kit.profile.domain.useCases.SettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MockSettingsUseCase : SettingsUseCase {
    private val account = MutableStateFlow<Account?>(Account(name = "Анна", phone = "+7 900 000-00-00"))

    override fun observeAccount(): Flow<Account?> = account

    override suspend fun saveName(name: String) = account.update { it?.copy(name = name.trim()) }

    override suspend fun signOut() = account.update { null }

    override suspend fun deleteAccount() = account.update { null }
}
