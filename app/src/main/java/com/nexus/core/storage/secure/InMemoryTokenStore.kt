package com.nexus.core.storage.secure

class InMemoryTokenStore : TokenStore {
    private val bbsTokens = linkedMapOf<Long, String>()
    private val BoxAccessTokens = linkedMapOf<BoxAccessTokenKey, String>()

    override suspend fun saveBbsToken(accountId: Long, token: String) {
        bbsTokens[accountId] = token
    }

    override suspend fun getBbsToken(accountId: Long): String? = bbsTokens[accountId]

    override suspend fun deleteBbsToken(accountId: Long) {
        bbsTokens.remove(accountId)
    }

    override suspend fun saveBoxAccessToken(key: BoxAccessTokenKey, token: String) {
        BoxAccessTokens[key] = token
    }

    override suspend fun getBoxAccessToken(key: BoxAccessTokenKey): String? = BoxAccessTokens[key]

    override suspend fun deleteBoxAccessToken(key: BoxAccessTokenKey) {
        BoxAccessTokens.remove(key)
    }
}
