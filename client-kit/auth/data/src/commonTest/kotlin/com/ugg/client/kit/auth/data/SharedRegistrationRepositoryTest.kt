package com.ugg.client.kit.auth.data

import com.ugg.client.infrastructure.firebase.auth.AuthUser
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneVerification
import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.auth.data.repositories.SharedRegistrationRepository
import com.ugg.client.kit.auth.domain.exceptions.SignInUnavailableException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedRegistrationRepositoryTest {

    @Test
    fun nameIsAddedToTheExistingProfile() = runTest {
        val users = FakeUsers(mapOf("u" to UserDto(phone = "+79001234567", size = UserDto.UserSizeDto("38", "standard"))))

        SharedRegistrationRepository(FakeAuth(AuthUser("u", "+79001234567")), users).saveName("  Анна ")

        val saved = users.saved.getValue("u")
        assertEquals("Анна", saved.name)
        assertEquals("+79001234567", saved.phone)
        assertEquals("38", saved.size?.label)
    }

    @Test
    fun missingProfileIsCreatedWithThePhone() = runTest {
        val users = FakeUsers()

        SharedRegistrationRepository(FakeAuth(AuthUser("u", "+79001234567")), users).saveName("Анна")

        assertEquals(UserDto(name = "Анна", phone = "+79001234567"), users.saved.getValue("u"))
    }

    @Test
    fun guestCannotSaveAName() = runTest {
        assertFailsWith<SignInUnavailableException> {
            SharedRegistrationRepository(FakeAuth(null), FakeUsers()).saveName("Анна")
        }
    }

    private class FakeAuth(override val currentUser: AuthUser?) : PhoneAuthService {
        override fun observeUser(): Flow<AuthUser?> = flowOf(currentUser)
        override suspend fun sendCode(phone: String): PhoneVerification = error("not used")
        override suspend fun verify(verificationId: String, code: String): AuthUser = error("not used")
        override suspend fun signOut() = Unit
    }

    private class FakeUsers(existing: Map<String, UserDto> = emptyMap()) : UserService {
        val saved = existing.toMutableMap()
        override fun observeUser(uid: String): Flow<UserDto?> = flowOf(saved[uid])
        override suspend fun getUser(uid: String): UserDto? = saved[uid]
        override suspend fun saveUser(uid: String, user: UserDto) { saved[uid] = user }
        override fun observeAddresses(uid: String): Flow<List<AddressDto>> = flowOf(emptyList())
        override suspend fun saveAddress(uid: String, address: AddressDto) = ""
        override suspend fun deleteAddress(uid: String, addressId: String) = Unit
    }
}
