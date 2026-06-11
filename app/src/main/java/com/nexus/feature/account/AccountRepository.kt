package com.nexus.feature.account

import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.WuwaAccount

interface AccountRepository {
    suspend fun bindWuwaAccount(token: String, nickname: String?): OperationResult<WuwaAccount>
    suspend fun bindPgrAccount(token: String, nickname: String?): OperationResult<WuwaAccount>
    suspend fun updateWuwaAccountRemark(
        accountId: Long,
        nickname: String?,
    ): OperationResult<WuwaAccount>
    suspend fun deleteWuwaAccount(accountId: Long): OperationResult<Unit>
}
