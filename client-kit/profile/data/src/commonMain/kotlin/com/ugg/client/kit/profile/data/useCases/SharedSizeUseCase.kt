package com.ugg.client.kit.profile.data.useCases

import com.ugg.client.infrastructure.firebase.auth.PhoneAuthService
import com.ugg.client.infrastructure.firebase.firestore.dto.UserDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.infrastructure.firebase.firestore.services.UserService
import com.ugg.client.kit.core.data.content.translatingContentErrors
import com.ugg.client.kit.profile.domain.entities.FootSize
import com.ugg.client.kit.profile.domain.entities.SizeOptions
import com.ugg.client.kit.profile.domain.exceptions.SignInRequiredException
import com.ugg.client.kit.profile.domain.useCases.SizeUseCase

/** Sizes and heights from `products/classic`; the choice goes to `users/{uid}.size`. */
class SharedSizeUseCase(
    private val auth: PhoneAuthService,
    private val users: UserService,
    private val catalog: CatalogService,
) : SizeUseCase {

    override suspend fun options(): SizeOptions = translatingContentErrors {
        val product = catalog.product()
        SizeOptions(
            sizes = product.sizes.map { it.label },
            heights = product.heights.map { SizeOptions.HeightOption(id = it.id, name = it.name, value = "${it.heightCm} см") },
        )
    }

    override suspend fun current(): FootSize = translatingContentErrors {
        val size = users.getUser(uid())?.size
        FootSize(
            size = size?.label?.takeIf { it.isNotBlank() },
            heightId = size?.heightId?.takeIf { it.isNotBlank() },
            footLengthCm = size?.footLengthCm,
        )
    }

    override suspend fun save(size: FootSize) {
        val uid = uid()
        translatingContentErrors {
            val user = users.getUser(uid) ?: UserDto(phone = auth.currentUser?.phone.orEmpty())
            val saved = UserDto.UserSizeDto(
                label = size.size.orEmpty(),
                heightId = size.heightId.orEmpty(),
                footLengthCm = size.footLengthCm,
            )
            users.saveUser(uid, user.copy(size = saved))
        }
    }

    private fun uid() = auth.currentUser?.uid ?: throw SignInRequiredException()
}
