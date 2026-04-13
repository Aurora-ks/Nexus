package com.nexus.core.network

import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import com.nexus.core.storage.preferences.DeviceIdentityStore

class KuroHeaderProvider(
    private val appContext: Context,
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
            "User-Agent" to "${WebSettings.getDefaultUserAgent(appContext)} Kuro/2.11.0 KuroGameBox/2.11.0",
            "X-Requested-With" to "com.kurogame.kjq",
            "source" to "android",
            "token" to token,
            "ip" to ipAddress,
            "devCode" to identity.devCode,
            "Origin" to "https://web-static.kurobbs.com",
            "Sec-Fetch-Site" to "same-site",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Dest" to "empty",
            "sec-ch-ua-mobile" to "?1",
            "sec-ch-ua-platform" to "Android",

        )
    }
}
