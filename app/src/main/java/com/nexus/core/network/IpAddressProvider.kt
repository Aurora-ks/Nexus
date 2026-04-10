package com.nexus.core.network

import java.net.Inet4Address
import java.net.NetworkInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class IpAddressProvider {
    open suspend fun getCurrentIp(): String = withContext(Dispatchers.IO) {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()

        val siteLocalAddress = networkInterfaces.asSequence()
            .filter { networkInterface ->
                networkInterface.isUp && !networkInterface.isLoopback && !networkInterface.isVirtual
            }
            .flatMap { networkInterface -> networkInterface.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { address -> !address.isLoopbackAddress && address.isSiteLocalAddress }

        if (siteLocalAddress != null) {
            return@withContext siteLocalAddress.hostAddress.orEmpty()
        }

        networkInterfaces.asSequence()
            .filter { networkInterface ->
                networkInterface.isUp && !networkInterface.isLoopback && !networkInterface.isVirtual
            }
            .flatMap { networkInterface -> networkInterface.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { address -> !address.isLoopbackAddress }
            ?.hostAddress
            ?: "127.0.0.1"
    }
}
