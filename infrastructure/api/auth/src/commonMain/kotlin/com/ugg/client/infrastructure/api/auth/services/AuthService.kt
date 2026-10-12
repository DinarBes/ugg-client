package com.ugg.client.infrastructure.api.auth.services

import com.ugg.client.infrastructure.api.auth.TemplateAuth
import com.ugg.client.infrastructure.api.auth.TemplateTokens
import com.ugg.client.infrastructure.api.auth.context.AuthEvent

class AuthService(private val auth: TemplateAuth) {
    private val client by auth.client
    private val context by auth.context

    suspend fun initiate(phone: String): String {
        // TODO: replace with real API call when backend is ready
        // val response = client.post("api/auth/initiate") {
        //     contentType(ContentType.Application.Json)
        //     setBody(mapOf("phone" to phone))
        // }
        // return response.body<InitiateResponse>().sessionId
        return "stub-session-id"
    }

    suspend fun login(otp: String, sessionId: String) {
        // TODO: replace with real API call when backend is ready. Persist the returned tokens
        // through auth.store(...) — the write seam wired at the composition root, so this feature
        // never touches the keystore directly.
        // val response = client.submitForm(url = "oauth2/token", formParameters = Parameters.build {
        //     append("grant_type", "...")
        //     append("session_id", sessionId)
        //     append("code", otp)
        // })
        // val tokens: TokenResponse = response.body()
        // auth.store(TemplateTokens(tokens.accessToken, tokens.refreshToken))
        context.trySend(AuthEvent.Login)
    }
}
