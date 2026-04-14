package com.nexus.core.storage.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedTokenStore(
    context: Context,
) : TokenStore {
    private val appContext = context.applicationContext

    private val preferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun saveBbsToken(accountId: Long, token: String) {
        preferences.edit()
            .putString(bbsTokenKey(accountId), token)
            .apply()
    }

    override suspend fun getBbsToken(accountId: Long): String? = preferences.getString(bbsTokenKey(accountId), null)

    override suspend fun deleteBbsToken(accountId: Long) {
        preferences.edit()
            .remove(bbsTokenKey(accountId))
            .apply()
    }

    override suspend fun saveBoxAccessToken(key: BoxAccessTokenKey, token: String) {
        preferences.edit()
            .putString(BoxAccessTokenKey(key), token)
            .apply()
    }

    override suspend fun getBoxAccessToken(key: BoxAccessTokenKey): String? =
        preferences.getString(BoxAccessTokenKey(key), null)

    override suspend fun deleteBoxAccessToken(key: BoxAccessTokenKey) {
        preferences.edit()
            .remove(BoxAccessTokenKey(key))
            .apply()
    }

    private fun bbsTokenKey(accountId: Long): String = "bbs_token_$accountId"

    private fun BoxAccessTokenKey(key: BoxAccessTokenKey): String =
        "battle_token_${key.gameId}_${key.userId}_${key.roleId}"

    private companion object {
        const val PREFS_NAME = "nexus_secure_tokens"
    }
}
