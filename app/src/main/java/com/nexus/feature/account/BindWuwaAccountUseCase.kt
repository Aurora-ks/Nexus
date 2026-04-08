package com.nexus.feature.account

import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.WuwaAccount

class BindWuwaAccountUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(token: String, nickname: String?): OperationResult<WuwaAccount> {
        return repository.bindWuwaAccount(token, nickname)
    }
}
