package com.nexus.core.storage.preferences

import android.content.Context
import java.util.UUID

class SharedPreferencesDeviceIdentityStore(
    context: Context,
) : DeviceIdentityStore {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun getOrCreate(): DeviceIdentity {
        val savedDevCode = preferences.getString(KEY_DEV_CODE, null)
        val savedDid = preferences.getString(KEY_DID, null)
        if (savedDevCode != null && savedDid != null) {
            return DeviceIdentity(devCode = savedDevCode, did = savedDid)
        }

        val identity = DeviceIdentity(
            devCode = savedDevCode ?: generateDevCode(),
            did = savedDid ?: UUID.randomUUID().toString(),
        )
        preferences.edit()
            .putString(KEY_DEV_CODE, identity.devCode)
            .putString(KEY_DID, identity.did)
            .apply()
        return identity
    }

    private fun generateDevCode(): String = UUID.randomUUID().toString().replace("-", "").uppercase()

    private companion object {
        const val PREFS_NAME = "nexus_device_identity"
        const val KEY_DEV_CODE = "dev_code"
        const val KEY_DID = "did"
    }
}
