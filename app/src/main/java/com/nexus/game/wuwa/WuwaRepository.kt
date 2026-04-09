package com.nexus.game.wuwa

import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.WuwaAccount

interface WuwaRepository {
    suspend fun bindAccount(token: String, nickname: String?): OperationResult<WuwaAccount>
    suspend fun updateAccountRemark(accountId: Long, nickname: String?): OperationResult<WuwaAccount>
    suspend fun deleteAccount(accountId: Long): OperationResult<Unit>
    suspend fun syncAccounts(): OperationResult<List<DashboardCardModel>>
}
