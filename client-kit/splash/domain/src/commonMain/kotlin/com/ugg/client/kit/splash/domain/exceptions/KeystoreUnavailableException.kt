package com.ugg.client.kit.splash.domain.exceptions

/**
 * The initial flow could not be decided because the stored credentials were unreadable.
 *
 * Means "ask again later", not "the user is signed out" — the splash screen stays put instead of
 * guessing.
 */
class KeystoreUnavailableException : Exception(
    "Credentials could not be read; the device may not have been unlocked since boot."
)
