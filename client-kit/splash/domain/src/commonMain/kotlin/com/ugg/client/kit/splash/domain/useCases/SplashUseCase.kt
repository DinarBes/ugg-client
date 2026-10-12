package com.ugg.client.kit.splash.domain.useCases

import com.ugg.client.kit.splash.domain.entities.SplashFlow
import kotlinx.coroutines.flow.Flow

interface SplashUseCase {
    /** @throws com.ugg.client.kit.splash.domain.exceptions.KeystoreUnavailableException */
    suspend fun getInitialFlow(): SplashFlow

    fun flows(): Flow<SplashFlow>
}
