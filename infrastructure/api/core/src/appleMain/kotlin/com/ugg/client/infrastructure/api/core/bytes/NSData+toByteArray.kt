package com.ugg.client.infrastructure.api.core.bytes

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
val NSData.byteArray: ByteArray
    get() = ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }

@Suppress("unused")
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
val ByteArray.nsData: NSData
    get() = usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong())
    }
