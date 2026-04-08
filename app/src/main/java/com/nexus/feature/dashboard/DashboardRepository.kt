package com.nexus.feature.dashboard

import com.nexus.core.model.OperationResult
import com.nexus.game.wuwa.model.DashboardCardModel

interface DashboardRepository {
    suspend fun sync(): OperationResult<List<DashboardCardModel>>
}
