package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.services.OrderService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.profile.data.mappers.profileFrom
import com.ugg.client.kit.profile.domain.entities.Profile
import com.ugg.client.kit.profile.domain.useCases.ProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * The profile follows Firebase Auth: a guest gets `null`; a signed-in user gets `users/{uid}`
 * combined with their orders, both live.
 */
class SharedProfileUseCase(
    private val auth: PhoneAuthService,
    private val users: UserService,
    private val orders: OrderService,
) : ProfileUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<Profile?> = auth.observeUser().flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            combine(users.observeUser(user.uid), orders.observeOrders(user.uid)) { profile, orders ->
                profileFrom(user, profile, orders)
            }.translatingContentErrors()
        }
    }

    override suspend fun signOut() = auth.signOut()
}
