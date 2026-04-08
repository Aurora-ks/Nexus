package com.nexus.feature.account

import com.nexus.core.model.OperationResult

class DeleteWuwaAccountUseCase(
    private val repository: AccountRepository,
) {
    suspend operator fun invoke(accountId: Long): OperationResult<Unit> {
        return repository.deleteWuwaAccount(accountId)
    }
}
