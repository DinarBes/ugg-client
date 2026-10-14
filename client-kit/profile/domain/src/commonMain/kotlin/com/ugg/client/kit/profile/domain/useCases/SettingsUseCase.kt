package com.ugg.client.kit.profile.domain.useCases

import com.ugg.client.kit.profile.domain.entities.Account
import kotlinx.coroutines.flow.Flow

interface SettingsUseCase {
    /** `null` for a guest. */
    fun observeAccount(): Flow<Account?>

    suspend fun saveName(name: String)

    suspend fun signOut()

    /**
     * Removes the profile with its addresses, then the account itself; the user ends up a guest.
     * Orders are kept — they are the workshop's records.
     */
    suspend fun deleteAccount()
}
