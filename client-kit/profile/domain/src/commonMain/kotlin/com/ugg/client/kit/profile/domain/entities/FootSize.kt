package com.ugg.client.kit.profile.domain.entities

/** The customer's size as saved in the profile; every part may be missing. */
data class FootSize(
    /** «38». */
    val size: String? = null,
    /** `standard` / `tall`. */
    val heightId: String? = null,
    /** Measured from the heel to the longest toe. */
    val footLengthCm: Double? = null,
)

/** What the size form offers, taken from the product so it never lists a size we do not sew. */
data class SizeOptions(
    val sizes: List<String> = emptyList(),
    val heights: List<HeightOption> = emptyList(),
) {
    /** «Стандарт · 22 см». */
    data class HeightOption(val id: String, val name: String, val value: String)
}
