package com.ugg.client.kit.core.domain

import org.kodein.di.DI
import org.kodein.di.instance

inline fun <reified T : Any> DI.resolve(): T {
    val value: T by instance()
    return value
}