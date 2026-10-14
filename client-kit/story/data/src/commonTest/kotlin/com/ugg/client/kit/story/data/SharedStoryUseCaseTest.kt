package com.ugg.client.kit.story.data

import com.ugg.client.infrastructure.firebase.firestore.FirestoreException
import com.ugg.client.infrastructure.firebase.firestore.dto.CheckoutContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.HomeContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.OnboardingContentDto
import com.ugg.client.infrastructure.firebase.firestore.dto.ProductDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StepDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoreDto
import com.ugg.client.infrastructure.firebase.firestore.dto.StoryContentDto
import com.ugg.client.infrastructure.firebase.firestore.services.CatalogService
import com.ugg.client.kit.core.domain.exceptions.ContentUnavailableException
import com.ugg.client.kit.story.data.useCases.SharedStoryUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SharedStoryUseCaseTest {

    @Test
    fun storyKeepsItsTextsAndSteps() = runTest {
        val story = SharedStoryUseCase(
            FakeCatalog(
                StoryContentDto(
                    kicker = "О мастерской",
                    title = "Сшито\nна Байкале",
                    steps = listOf(StepDto("01", "Овчина", "Выбираем шкуры")),
                    highlight = StoryContentDto.HighlightDto(kicker = "Почему Байкал", title = "Зима полгода", body = "…"),
                ),
            ),
        ).fetch()

        assertEquals("Сшито\nна Байкале", story.title)
        assertEquals("Овчина", story.steps.single().title)
        assertEquals("Зима полгода", story.highlight?.title)
    }

    @Test
    fun emptyHighlightIsHidden() = runTest {
        val story = SharedStoryUseCase(FakeCatalog(StoryContentDto(highlight = StoryContentDto.HighlightDto()))).fetch()

        assertNull(story.highlight)
    }

    @Test
    fun missingDocumentBecomesContentUnavailable() = runTest {
        val catalog = FakeCatalog(failure = FirestoreException(FirestoreException.Reason.NotFound))

        assertFailsWith<ContentUnavailableException> { SharedStoryUseCase(catalog).fetch() }
    }

    private class FakeCatalog(
        private val story: StoryContentDto = StoryContentDto(),
        private val failure: Exception? = null,
    ) : CatalogService {
        override suspend fun story() = failure?.let { throw it } ?: story
        override suspend fun product(id: String) = ProductDto()
        override suspend fun stores() = emptyList<StoreDto>()
        override suspend fun home() = HomeContentDto()
        override suspend fun onboarding() = OnboardingContentDto()
        override suspend fun checkout() = CheckoutContentDto()
    }
}
