package com.ugg.client.kit.profile.domain.useCases

import com.ugg.client.kit.profile.domain.entities.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileUseCase {
    /**
     * The signed-in user's profile, or `null` for a guest. Emits again on sign-in and sign-out,
     * when the profile is edited and when the workshop moves an order to the next stage.
     */
    fun observeProfile(): Flow<Profile?>

    suspend fun signOut()
}
