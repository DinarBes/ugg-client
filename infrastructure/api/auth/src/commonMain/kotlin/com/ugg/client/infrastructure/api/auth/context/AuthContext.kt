package com.ugg.client.infrastructure.api.auth.context

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class AuthContext {
    // Буфер обязателен: у MutableSharedFlow() без него tryEmit не может доставить значение
    // без приостановки и всегда возвращает false, молча теряя событие.
    val event: SharedFlow<AuthEvent>
        field = MutableSharedFlow(extraBufferCapacity = 1)

    suspend fun send(event: AuthEvent) = this.event.emit(event)

    fun trySend(event: AuthEvent) = this.event.tryEmit(event)
}
