package com.ugg.client.infrastructure.api.core.url

import io.ktor.http.Url
import platform.Foundation.NSURL

val Url.asNSURL: NSURL
    get() = NSURL(string = toString())

val NSURL.asUrl: Url
    get() = Url(urlString = toString())
