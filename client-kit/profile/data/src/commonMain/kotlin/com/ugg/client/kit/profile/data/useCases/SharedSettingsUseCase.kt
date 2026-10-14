package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthException
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.core.data.formatting.formatPhone
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import com.ugg.client.kit.profile.domain.entities.Account
import com.ugg.client.kit.profile.domain.exceptions.RecentSignInRequiredException
import com.ugg.client.kit.profile.domain.exceptions.SignInRequiredException
import com.ugg.client.kit.profile.domain.useCases.SettingsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SharedSettingsUseCase(
    private val auth: PhoneAuthService,
    private val users: UserService,
) : SettingsUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAccount(): Flow<Account?> = auth.observeUser().flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            users.observeUser(user.uid)
                .map { profile -> Account(name = profile?.name?.trim().orEmpty(), phone = (user.phone ?: profile?.phone.orEmpty()).formatPhone()) }
                .translatingContentErrors()
        }
    }

    override suspend fun saveName(name: String) {
        val user = auth.currentUser ?: throw SignInRequiredException()
        translatingContentErrors {
            val profile = users.getUser(user.uid) ?: UserDto(phone = user.phone.orEmpty())
            users.saveUser(user.uid, profile.copy(name = name.trim()))
        }
    }

    override suspend fun signOut() = auth.signOut()

    override suspend fun deleteAccount() {
        val user = auth.currentUser ?: throw SignInRequiredException()

        // The data first: once the account is gone, the security rules no longer let us touch it.
        translatingContentErrors { users.deleteUser(user.uid) }

        try {
            auth.deleteAccount()
        } catch (e: PhoneAuthException) {
            throw when (e.reason) {
                PhoneAuthException.Reason.RecentLoginRequired -> RecentSignInRequiredException()
                PhoneAuthException.Reason.Network -> NoConnectionException(e)
                else -> Exception("Не получилось удалить аккаунт. Попробуйте ещё раз", e)
            }
        }
    }
}
