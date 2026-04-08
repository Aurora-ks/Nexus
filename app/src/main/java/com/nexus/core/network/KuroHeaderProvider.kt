package com.nexus.core.network

import java.util.UUID

class KuroHeaderProvider {
    fun nativeHeaders(token: String, devCode: String = generateDevCode(), did: String = UUID.randomUUID().toString()): Map<String, String> {
        return mapOf(
            "User-Agent" to ApiConstants.NativeUserAgent,
            "X-Requested-With" to ApiConstants.RequestedWith,
            "source" to ApiConstants.AndroidSource,
            "token" to token,
            "devCode" to devCode,
            "did" to did,
            "Origin" to "https://web-static.kurobbs.com",
        )
    }

    fun webHeaders(token: String, devCode: String = "127.0.0.1, ${ApiConstants.WebUserAgent}"): Map<String, String> {
        return mapOf(
            "User-Agent" to ApiConstants.WebUserAgent,
            "X-Requested-With" to ApiConstants.RequestedWith,
            "source" to ApiConstants.AndroidSource,
            "token" to token,
            "devCode" to devCode,
            "sec-ch-ua-platform" to "\"Android\"",
            "Origin" to "https://web-static.kurobbs.com",
        )
    }

    private fun generateDevCode(): String = UUID.randomUUID().toString().replace("-", "").uppercase()
}
