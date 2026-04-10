package com.nexus.core.network

import android.os.Build
import com.nexus.core.storage.preferences.DeviceIdentityStore

class KuroHeaderProvider(
    private val deviceIdentityStore: DeviceIdentityStore,
    private val ipAddressProvider: IpAddressProvider,
) {
    suspend fun nativeHeaders(token: String): Map<String, String> {
        val identity = deviceIdentityStore.getOrCreate()
        val ipAddress = ipAddressProvider.getCurrentIp()
        return mapOf(
            "User-Agent" to "okhttp/3.11.0",
            "source" to "android",
            "token" to token,
            "ip" to ipAddress,
            "version" to "2.11.0",
            "versionCode" to "21100",
            "osVersion" to Build.VERSION.SDK_INT.toString(),
            "model" to Build.MODEL,
            "lang" to "zh-Hans",
            "channelId" to "2",
            "countryCode" to "CN",
            "devCode" to identity.devCode,
            "did" to identity.did,
        )
    }
    suspend fun webHeaders(token: String): Map<String, String> {
        val identity = deviceIdentityStore.getOrCreate()
        val ipAddress = ipAddressProvider.getCurrentIp()
        return mapOf(
            "User-Agent" to ApiConstants.WebUserAgent,
            "X-Requested-With" to ApiConstants.RequestedWith,
            "source" to "android",
            "token" to token,
            "ip" to ipAddress,
            "devCode" to identity.devCode,
            "sec-ch-ua-platform" to "\"Android\"",
            "Origin" to "https://web-static.kurobbs.com",
        )
    }
}
