@file:OptIn(ExperimentalTime::class)
@file:Suppress("unused")

package com.ugg.client.infrastructure.api.core.datetime

import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun NSDate.toInstant(): Instant =
    Instant.fromEpochMilliseconds((this.timeIntervalSince1970 * 1000.0).toLong())

fun Instant.toNSDate(): NSDate =
    NSDate.dateWithTimeIntervalSince1970(this.epochSeconds.toDouble() + this.nanosecondsOfSecond / 1_000_000_000.0)
