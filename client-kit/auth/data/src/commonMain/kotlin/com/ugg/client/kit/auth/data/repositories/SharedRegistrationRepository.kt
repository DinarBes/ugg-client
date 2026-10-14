package com.ugg.client.kit.auth.data.repositories

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.auth.domain.exceptions.SignInUnavailableException
import com.ugg.client.kit.auth.domain.useCases.RegistrationUseCase
import com.ugg.client.kit.core.data.content.translatingContentErrors

/** Writes the name into `users/{uid}`, keeping what the document already holds. */
class SharedRegistrationRepository(
    private val phoneAuth: PhoneAuthService,
    private val users: UserService,
) : RegistrationUseCase {

    override suspend fun saveName(name: String) {
        val user = phoneAuth.currentUser ?: throw SignInUnavailableException()

        translatingContentErrors {
            val profile = users.getUser(user.uid) ?: UserDto(phone = user.phone.orEmpty())
            users.saveUser(user.uid, profile.copy(name = name.trim()))
        }
    }
}
