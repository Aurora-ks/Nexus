package com.nexus.core.storage.secure

interface TokenStore {
    suspend fun save(accountId: Long, token: String)
    suspend fun get(accountId: Long): String?
    suspend fun delete(accountId: Long)
}
