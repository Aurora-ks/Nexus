package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.WuwaAccount

interface WuwaAccountStore {
    suspend fun save(account: WuwaAccount): WuwaAccount
    suspend fun getAccount(accountId: Long): WuwaAccount?
    suspend fun updateRemark(accountId: Long, nickname: String?): WuwaAccount?
    suspend fun getAccounts(): List<WuwaAccount>
    suspend fun delete(accountId: Long)
}
