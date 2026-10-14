package com.ugg.client.kit.profile.data

import com.ugg.client.infrastructure.firebase.auth.AuthUser
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthException
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneVerification
import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.profile.data.useCases.SharedSettingsUseCase
import com.ugg.client.kit.profile.domain.exceptions.RecentSignInRequiredException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SharedSettingsUseCaseTest {

    @Test
    fun accountShowsTheNameAndTheFormattedPhone() = runTest {
        val useCase = SharedSettingsUseCase(FakeAuth(), FakeUsers(mapOf("u" to UserDto(name = "Анна"))))

        val account = useCase.observeAccount().first()

        assertEquals("Анна", account?.name)
        assertEquals("+7 900 123-45-67", account?.phone)
    }

    @Test
    fun savingTheNameKeepsTheSize() = runTest {
        val users = FakeUsers(mapOf("u" to UserDto(name = "Анна", size = UserDto.UserSizeDto("38", "standard"))))

        SharedSettingsUseCase(FakeAuth(), users).saveName("  Аня ")

        assertEquals("Аня", users.saved.value.getValue("u").name)
        assertEquals("38", users.saved.value.getValue("u").size?.label)
    }

    @Test
    fun deletingRemovesTheDataThenTheAccount() = runTest {
        val log = mutableListOf<String>()
        val auth = FakeAuth(log = log)
        val users = FakeUsers(mapOf("u" to UserDto(name = "Анна")), log = log)
        val useCase = SharedSettingsUseCase(auth, users)

        useCase.deleteAccount()

        // The data first: after the account is gone, the rules would not let us delete it.
        assertEquals(listOf("deleteUser:u", "deleteAccount"), log)
        assertNull(useCase.observeAccount().first())
    }

    @Test
    fun staleSignInAsksToSignInAgain() = runTest {
        val auth = FakeAuth(deleteFailure = PhoneAuthException(PhoneAuthException.Reason.RecentLoginRequired))

        assertFailsWith<RecentSignInRequiredException> { SharedSettingsUseCase(auth, FakeUsers()).deleteAccount() }
    }

    private class FakeAuth(
        private val deleteFailure: PhoneAuthException? = null,
        private val log: MutableList<String> = mutableListOf(),
    ) : PhoneAuthService {
        private val user = MutableStateFlow<AuthUser?>(AuthUser("u", "+79001234567"))
        override fun observeUser(): Flow<AuthUser?> = user
        override val currentUser: AuthUser? get() = user.value
        override suspend fun sendCode(phone: String): PhoneVerification = error("not used")
        override suspend fun verify(verificationId: String, code: String): AuthUser = error("not used")
        override suspend fun signOut() = user.update { null }
        override suspend fun deleteAccount() {
            deleteFailure?.let { throw it }
            log += "deleteAccount"
            user.update { null }
        }
    }

    private class FakeUsers(
        existing: Map<String, UserDto> = emptyMap(),
        private val log: MutableList<String> = mutableListOf(),
    ) : UserService {
        val saved = MutableStateFlow(existing)
        override fun observeUser(uid: String): Flow<UserDto?> = saved.map { it[uid] }
        override suspend fun getUser(uid: String): UserDto? = saved.value[uid]
        override suspend fun saveUser(uid: String, user: UserDto) = saved.update { it + (uid to user) }
        override fun observeAddresses(uid: String): Flow<List<AddressDto>> = MutableStateFlow(emptyList())
        override suspend fun saveAddress(uid: String, address: AddressDto) = ""
        override suspend fun deleteAddress(uid: String, addressId: String) = Unit
        override suspend fun deleteUser(uid: String) {
            log += "deleteUser:$uid"
            saved.update { it - uid }
        }
    }
}
