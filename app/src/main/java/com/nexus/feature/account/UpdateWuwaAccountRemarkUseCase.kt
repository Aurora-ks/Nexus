package com.nexus.feature.account

import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.WuwaAccount

class UpdateWuwaAccountRemarkUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(accountId: Long, nickname: String?): OperationResult<WuwaAccount> {
        return repository.updateWuwaAccountRemark(accountId, nickname)
    }
}
