package com.ugg.client.kit.core.domain.exceptions

/** The screen could not load its content; [message] is ready to show. */
open class ContentUnavailableException(
    message: String = "Не удалось загрузить. Попробуйте ещё раз",
    cause: Throwable? = null,
) : Exception(message, cause)

/** No network and nothing cached yet. */
class NoConnectionException(cause: Throwable? = null) :
    ContentUnavailableException("Нет соединения. Проверьте интернет и попробуйте ещё раз", cause)
