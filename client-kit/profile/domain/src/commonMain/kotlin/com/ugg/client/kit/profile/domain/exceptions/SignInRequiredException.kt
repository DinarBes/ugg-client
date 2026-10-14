package com.ugg.client.kit.profile.domain.exceptions

/** The form needs an account to save to; the session ended meanwhile. */
class SignInRequiredException : Exception("Войдите по номеру телефона, чтобы сохранить")
