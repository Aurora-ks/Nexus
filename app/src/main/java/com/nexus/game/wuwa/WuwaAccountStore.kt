package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.WuwaAccount

interface WuwaAccountStore {
    suspend fun save(account: WuwaAccount): WuwaAccount
    suspend fun getAccounts(): List<WuwaAccount>
    suspend fun delete(accountId: Long)
}
