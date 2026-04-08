package com.nexus.core.storage.secure

class InMemoryTokenStore : TokenStore {
    private val tokens = linkedMapOf<Long, String>()

    override suspend fun save(accountId: Long, token: String) {
        tokens[accountId] = token
    }

    override suspend fun get(accountId: Long): String? = tokens[accountId]

    override suspend fun delete(accountId: Long) {
        tokens.remove(accountId)
    }
}
