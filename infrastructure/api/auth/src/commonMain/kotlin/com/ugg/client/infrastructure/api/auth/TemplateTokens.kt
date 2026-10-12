package com.ugg.client.infrastructure.api.auth

/**
 * The access/refresh pair the auth client works with.
 *
 * Carried across the store seam ([TemplateAuth.store]) so callers persist tokens as one typed
 * value rather than two loose strings. Distinct from Ktor's `BearerTokens`: this is the template's
 * own model, kept free of any Ktor dependency at the call site.
 */
data class TemplateTokens(
    val accessToken: String,
    val refreshToken: String,
)
