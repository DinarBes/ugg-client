package com.ugg.client.infrastructure.api.auth.dsl

import com.ugg.client.infrastructure.api.auth.TemplateTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The write side of the token seam: `storeTokens` / `clearTokens` are how the composition root
 * feeds a token store into the auth client without the API layer depending on one.
 */
class TemplateAuthConfigurationTest {

    @Test
    fun storeTokensBlockReceivesTheTokensVerbatim() {
        var captured: TemplateTokens? = null
        val configuration = TemplateAuthConfiguration().apply {
            storeTokens { captured = it }
        }

        configuration.storeTokensBlock(TemplateTokens("access", "refresh"))

        assertEquals(TemplateTokens("access", "refresh"), captured)
    }

    @Test
    fun clearTokensBlockFires() {
        var cleared = false
        val configuration = TemplateAuthConfiguration().apply {
            clearTokens { cleared = true }
        }

        configuration.clearTokensBlock()

        assertTrue(cleared)
    }

    @Test
    fun defaultBlocksAreNoOps() {
        val configuration = TemplateAuthConfiguration()

        // Unconfigured store/clear must not throw — an app may never wire them.
        configuration.storeTokensBlock(TemplateTokens("access", "refresh"))
        configuration.clearTokensBlock()
    }
}
