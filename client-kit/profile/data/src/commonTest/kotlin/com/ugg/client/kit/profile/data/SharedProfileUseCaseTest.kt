package com.ugg.client.kit.profile.data

import com.ugg.client.infrastructure.firebase.auth.AuthUser
import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.auth.PhoneVerification
import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.AddressDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDeliveryDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDraft
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderItemDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OrderStatus
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.OrderService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.core.domain.exceptions.NoConnectionException
import com.ugg.client.kit.profile.data.useCases.SharedProfileUseCase
import com.ugg.client.kit.profile.domain.entities.ProfileMenuItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SharedProfileUseCaseTest {

    @Test
    fun guestHasNoProfile() = runTest {
        val useCase = SharedProfileUseCase(FakeAuth(user = null), FakeUsers(), FakeOrders())

        assertNull(useCase.observeProfile().first())
    }

    @Test
    fun signedInUserGetsProfileWithTheOrderInProgress() = runTest {
        val useCase = SharedProfileUseCase(
            FakeAuth(AuthUser(uid = "u", phone = "+79001234567")),
            FakeUsers(UserDto(name = "Анна Королёва", size = UserDto.UserSizeDto("38", "standard"))),
            FakeOrders(
                listOf(
                    order(number = 1001, status = OrderStatus.Completed, createdAt = 1),
                    order(number = 1024, status = OrderStatus.Sewing, createdAt = 2, estimatedReadyAt = 1_760_745_000_000),
                ),
            ),
        )

        val profile = useCase.observeProfile().first()!!

        assertEquals("Анна Королёва", profile.name)
        assertEquals("АК", profile.initials)
        assertEquals("+7 900 123-45-67", profile.phone)
        assertEquals("38 · стандарт", profile.size)
        assertEquals("1 активный", profile.ordersSummary)
        assertEquals(ProfileMenuItem.Kind.Logout, profile.menu.last().kind)

        val order = profile.activeOrder!!
        assertEquals("Заказ № 1024", order.number)
        assertEquals("Каштан · 38", order.details)
        assertEquals("Ваша пара у мастера", order.title)
        assertEquals(2, order.currentStep)
        assertEquals(true, order.description.contains("октября"))
    }

    @Test
    fun freshAccountWithoutProfileDocument() = runTest {
        val useCase = SharedProfileUseCase(FakeAuth(AuthUser("u", "+79001234567")), FakeUsers(null), FakeOrders())

        val profile = useCase.observeProfile().first()!!

        assertEquals("Покупатель", profile.name)
        assertEquals("", profile.initials)
        assertNull(profile.size)
        assertNull(profile.activeOrder)
        assertEquals("пока нет", profile.ordersSummary)
    }

    @Test
    fun signingOutTurnsTheProfileIntoGuest() = runTest {
        val auth = FakeAuth(AuthUser("u", "+79001234567"))
        val useCase = SharedProfileUseCase(auth, FakeUsers(), FakeOrders())

        useCase.signOut()

        assertNull(useCase.observeProfile().first())
    }

    @Test
    fun noNetworkBecomesNoConnection() = runTest {
        val users = object : FakeUsers() {
            override fun observeUser(uid: String): Flow<UserDto?> = flow { throw FirestoreException(FirestoreException.Reason.Unavailable) }
        }
        val useCase = SharedProfileUseCase(FakeAuth(AuthUser("u", null)), users, FakeOrders())

        assertFailsWith<NoConnectionException> { useCase.observeProfile().first() }
    }

    private fun order(number: Long, status: OrderStatus, createdAt: Long, estimatedReadyAt: Long? = null) = OrderDto(
        id = "o$number",
        userId = "u",
        number = number,
        status = status,
        items = listOf(OrderItemDto(kind = OrderItemDto.KIND_BOOTS, shadeName = "Каштан", size = "38")),
        createdAt = createdAt,
        estimatedReadyAt = estimatedReadyAt,
    )

    private class FakeAuth(user: AuthUser?) : PhoneAuthService {
        private val user = MutableStateFlow(user)
        override fun observeUser(): Flow<AuthUser?> = user
        override val currentUser: AuthUser? get() = user.value
        override suspend fun sendCode(phone: String): PhoneVerification = error("not used")
        override suspend fun verify(verificationId: String, code: String): AuthUser = error("not used")
        override suspend fun signOut() = user.update { null }
        override suspend fun deleteAccount() = Unit
    }

    private open class FakeUsers(private val user: UserDto? = UserDto(name = "Анна")) : UserService {
        override fun observeUser(uid: String): Flow<UserDto?> = flowOf(user)
        override suspend fun getUser(uid: String): UserDto? = user
        override suspend fun saveUser(uid: String, user: UserDto) = Unit
        override fun observeAddresses(uid: String): Flow<List<AddressDto>> = flowOf(emptyList())
        override suspend fun saveAddress(uid: String, address: AddressDto) = ""
        override suspend fun deleteAddress(uid: String, addressId: String) = Unit
        override suspend fun deleteUser(uid: String) = Unit
    }

    private class FakeOrders(private val orders: List<OrderDto> = emptyList()) : OrderService {
        override fun observeOrders(uid: String): Flow<List<OrderDto>> = flowOf(orders)
        override fun observeActiveOrders(uid: String): Flow<List<OrderDto>> = flowOf(orders.filter { it.status.isActive })
        override suspend fun placeOrder(uid: String, draft: OrderDraft): OrderDto = error("not used")
    }
}
