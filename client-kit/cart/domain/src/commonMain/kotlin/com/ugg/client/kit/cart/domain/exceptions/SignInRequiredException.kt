package com.ugg.client.kit.cart.domain.exceptions

/** Checkout needs a signed-in user: orders are tied to the phone number. */
class SignInRequiredException : Exception("Войдите по номеру телефона, чтобы оформить заказ")
