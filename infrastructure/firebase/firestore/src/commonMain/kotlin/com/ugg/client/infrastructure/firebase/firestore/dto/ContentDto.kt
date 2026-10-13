package com.ugg.client.infrastructure.firebase.firestore.dto

import kotlinx.serialization.Serializable

/** `content/home`. Price and lead time of the hero come from the product. */
@Serializable
data class HomeContentDto(
    val heroKicker: String = "",
    val heroDescription: String = "",
    val stats: List<StatDto> = emptyList(),
    val craftSteps: List<StepDto> = emptyList(),
    val quote: QuoteDto? = null,
    val care: CareDto? = null,
) {
    @Serializable
    data class StatDto(val value: String = "", val caption: String = "")

    @Serializable
    data class QuoteDto(val text: String = "", val author: String = "", val role: String = "")

    @Serializable
    data class CareDto(val kicker: String = "", val title: String = "", val readMinutes: Int = 0)
}

/** `content/story`. */
@Serializable
data class StoryContentDto(
    val kicker: String = "",
    val title: String = "",
    val intro: String = "",
    val stepsTitle: String = "",
    val steps: List<StepDto> = emptyList(),
    val highlight: HighlightDto? = null,
) {
    @Serializable
    data class HighlightDto(val kicker: String = "", val title: String = "", val body: String = "")
}

/** A numbered step: «01 · Раскрой». Shared by the home and the story screens. */
@Serializable
data class StepDto(val number: String = "", val title: String = "", val description: String = "")

/** `content/onboarding`. */
@Serializable
data class OnboardingContentDto(
    val slides: List<SlideDto> = emptyList(),
) {
    @Serializable
    data class SlideDto(
        val id: String = "",
        val overline: String = "",
        val title: String = "",
        val description: String = "",
    )
}

/** `content/checkout`: what the shop sells besides the boots and how it delivers. */
@Serializable
data class CheckoutContentDto(
    val extras: List<ExtraDto> = emptyList(),
    val deliveries: List<DeliveryDto> = emptyList(),
    val giftWrap: GiftWrapDto = GiftWrapDto(),
) {
    @Serializable
    data class ExtraDto(val id: String = "", val title: String = "", val caption: String = "", val price: Int = 0)

    @Serializable
    data class DeliveryDto(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val price: Int = 0,
        /** The price is the lower bound: «от 450 ₽». */
        val isFromPrice: Boolean = false,
        /** Set for pickup: the store the order is collected from. */
        val storeId: String? = null,
    )

    @Serializable
    data class GiftWrapDto(val title: String = "", val description: String = "", val price: Int = 0)
}
