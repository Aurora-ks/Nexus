package com.nexus.core.storage.preferences

data class DeviceIdentity(
    val devCode: String,
    val did: String,
)

interface DeviceIdentityStore {
    fun getOrCreate(): DeviceIdentity
}
