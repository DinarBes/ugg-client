package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** The stub profile; «Выйти» turns the screen into the guest one until the app restarts. */
class MockProfileUseCase : ProfileUseCase {
    private val profile = MutableStateFlow<Profile?>(Profile.mock)

    override fun observeProfile(): Flow<Profile?> = profile

    override suspend fun signOut() = profile.update { null }
}
