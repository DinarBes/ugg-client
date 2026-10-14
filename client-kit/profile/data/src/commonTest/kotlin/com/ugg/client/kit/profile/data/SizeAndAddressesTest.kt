package com.ugg.client.kit.profile.data

import com.ugg.client.infrastructure.firebase.auth.AuthUser
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneVerification
import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.CareContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.profile.data.mappers.toSummary
import com.ugg.client.kit.profile.data.useCases.SharedAddressesUseCase
import com.ugg.client.kit.profile.data.useCases.SharedSizeUseCase
import com.ugg.client.kit.profile.domain.entities.Address
import com.ugg.client.kit.profile.domain.entities.FootSize
import com.ugg.client.kit.profile.domain.exceptions.SignInRequiredException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SizeAndAddressesTest {
    private val auth = FakeAuth(AuthUser("u", "+79001234567"))

    // ───── size ─────

    @Test
    fun sizeOptionsComeFromTheProduct() = runTest {
        val options = SharedSizeUseCase(auth, FakeUsers(), FakeCatalog()).options()

        assertEquals(listOf("37", "38"), options.sizes)
        assertEquals("22 см", options.heights.single().value)
    }

    @Test
    fun savingTheSizeKeepsTheRestOfTheProfile() = runTest {
        val users = FakeUsers(mapOf("u" to UserDto(name = "Анна", phone = "+79001234567")))
        val useCase = SharedSizeUseCase(auth, users, FakeCatalog())

        useCase.save(FootSize(size = "38", heightId = "standard", footLengthCm = 24.5))

        val saved = users.saved.getValue("u")
        assertEquals("Анна", saved.name)
        assertEquals(UserDto.UserSizeDto("38", "standard", 24.5), saved.size)
        assertEquals(FootSize("38", "standard", 24.5), useCase.current())
    }

    @Test
    fun guestCannotSaveASize() = runTest {
        assertFailsWith<SignInRequiredException> {
            SharedSizeUseCase(FakeAuth(null), FakeUsers(), FakeCatalog()).save(FootSize(size = "38"))
        }
    }

    @Test
    fun sizeSummaryForTheProfile() {
        assertEquals("38 · стандарт", UserDto.UserSizeDto("38", "standard").toSummary())
        assertEquals("стопа 24,5 см", UserDto.UserSizeDto(footLengthCm = 24.5).toSummary())
        assertEquals("стопа 25 см", UserDto.UserSizeDto(footLengthCm = 25.0).toSummary())
        assertNull(UserDto.UserSizeDto().toSummary())
    }

    // ───── addresses ─────

    @Test
    fun firstAddressBecomesTheDefault() = runTest {
        val useCase = SharedAddressesUseCase(auth, FakeUsers())

        useCase.save(Address(city = "Иркутск", street = "ул. Ленина, 12"))

        assertEquals(true, useCase.observeAddresses().first().single().isDefault)
    }

    @Test
    fun makingAnotherAddressDefaultTakesTheMarkFromTheFirst() = runTest {
        val useCase = SharedAddressesUseCase(auth, FakeUsers())
        val home = useCase.save(Address(city = "Иркутск", street = "ул. Ленина, 12"))
        val work = useCase.save(Address(city = "Иркутск", street = "ул. Карла Маркса, 1", isDefault = true))

        val addresses = useCase.observeAddresses().first()

        assertEquals(listOf(work, home), addresses.map { it.id })
        assertEquals(listOf(true, false), addresses.map { it.isDefault })
    }

    @Test
    fun deletingTheDefaultPassesTheMark() = runTest {
        val useCase = SharedAddressesUseCase(auth, FakeUsers())
        val home = useCase.save(Address(city = "Иркутск", street = "ул. Ленина, 12"))
        val dacha = useCase.save(Address(city = "Листвянка", street = "ул. Береговая, 3"))

        useCase.delete(home)

        val left = useCase.observeAddresses().first().single()
        assertEquals(dacha, left.id)
        assertEquals(true, left.isDefault)
    }

    @Test
    fun editingKeepsTheIdAndTrimsFields() = runTest {
        val useCase = SharedAddressesUseCase(auth, FakeUsers())
        val id = useCase.save(Address(city = "Иркутск", street = "ул. Ленина, 12"))

        useCase.save(useCase.address(id)!!.copy(flat = " 5 "))

        val edited = useCase.address(id)!!
        assertEquals("5", edited.flat)
        assertEquals("Иркутск, ул. Ленина, 12, кв. 5", edited.summary)
    }

    @Test
    fun guestHasNoAddresses() = runTest {
        assertEquals(emptyList(), SharedAddressesUseCase(FakeAuth(null), FakeUsers()).observeAddresses().first())
    }

    // ───── fakes ─────

    private class FakeAuth(user: AuthUser?) : PhoneAuthService {
        private val user = MutableStateFlow(user)
        override fun observeUser(): Flow<AuthUser?> = user
        override val currentUser: AuthUser? get() = user.value
        override suspend fun sendCode(phone: String): PhoneVerification = error("not used")
        override suspend fun verify(verificationId: String, code: String): AuthUser = error("not used")
        override suspend fun signOut() = user.update { null }
        override suspend fun deleteAccount() = Unit
    }

    private class FakeUsers(existing: Map<String, UserDto> = emptyMap()) : UserService {
        val saved = existing.toMutableMap()
        private val addresses = MutableStateFlow<Map<String, AddressDto>>(emptyMap())
        private var ids = 0

        override fun observeUser(uid: String): Flow<UserDto?> = flowOf(saved[uid])
        override suspend fun getUser(uid: String): UserDto? = saved[uid]
        override suspend fun saveUser(uid: String, user: UserDto) { saved[uid] = user }
        override fun observeAddresses(uid: String): Flow<List<AddressDto>> = addresses.map { it.values.toList() }
        override suspend fun saveAddress(uid: String, address: AddressDto): String {
            val id = address.id.ifEmpty { "a${++ids}" }
            addresses.update { it + (id to address.copy(id = id)) }
            return id
        }
        override suspend fun deleteAddress(uid: String, addressId: String) = addresses.update { it - addressId }
        override suspend fun deleteUser(uid: String) = Unit
    }

    private class FakeCatalog : CatalogService {
        override suspend fun product(id: String) = ProductDto(
            sizes = listOf(ProductDto.SizeDto("37"), ProductDto.SizeDto("38")),
            heights = listOf(ProductDto.HeightDto("standard", "Стандарт", 22)),
        )
        override suspend fun stores() = emptyList<StoreDto>()
        override suspend fun home() = HomeContentDto()
        override suspend fun story() = StoryContentDto()
        override suspend fun onboarding() = OnboardingContentDto()
        override suspend fun checkout() = CheckoutContentDto()
        override suspend fun care() = CareContentDto()
    }
}
