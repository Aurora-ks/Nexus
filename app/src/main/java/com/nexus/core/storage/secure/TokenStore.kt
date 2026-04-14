package com.nexus.core.storage.secure

data class BoxAccessTokenKey(
    val userId: String,
    val roleId: String,
    val gameId: Int,
)

interface TokenStore {
    suspend fun saveBbsToken(accountId: Long, token: String)
    suspend fun getBbsToken(accountId: Long): String?
    suspend fun deleteBbsToken(accountId: Long)

    suspend fun saveBoxAccessToken(key: BoxAccessTokenKey, token: String)
    suspend fun getBoxAccessToken(key: BoxAccessTokenKey): String?
    suspend fun deleteBoxAccessToken(key: BoxAccessTokenKey)
}
