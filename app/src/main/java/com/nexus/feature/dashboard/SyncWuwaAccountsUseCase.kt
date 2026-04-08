package com.nexus.feature.dashboard

import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.DashboardCardModel

class SyncWuwaAccountsUseCase(
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(): OperationResult<List<DashboardCardModel>> {
        return repository.sync()
    }
}
