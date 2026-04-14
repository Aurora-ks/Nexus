package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.WuwaAccount

class InMemoryWuwaAccountStore : WuwaAccountStore {
    private val accounts = linkedMapOf<Long, WuwaAccount>()
    private var nextId = 1L

    override suspend fun save(account: WuwaAccount): WuwaAccount {
        val saved = if (account.id == 0L) {
            account.copy(id = nextId++)
        } else {
            account
        }
        accounts[saved.id] = saved
        return saved
    }

    override suspend fun getAccount(accountId: Long): WuwaAccount? = accounts[accountId]

    override suspend fun updateRemark(accountId: Long, nickname: String?): WuwaAccount? {
        val account = accounts[accountId] ?: return null
        val updated = account.copy(nickname = nickname)
        accounts[accountId] = updated
        return updated
    }

    override suspend fun getAccounts(): List<WuwaAccount> = accounts.values.toList()

    override suspend fun delete(accountId: Long) {
        accounts.remove(accountId)
    }
}
