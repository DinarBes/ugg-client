package com.ugg.client.infrastructure.api.core.uuid

import platform.Foundation.NSUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val Uuid.nsUuid: NSUUID
    get() = NSUUID(uUIDString = toString())

@OptIn(ExperimentalUuidApi::class)
val NSUUID.uuid: Uuid
    get() = Uuid.parse(UUIDString)
