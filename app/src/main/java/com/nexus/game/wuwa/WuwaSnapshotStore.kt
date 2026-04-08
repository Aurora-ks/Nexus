package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.DashboardCardModel

interface WuwaSnapshotStore {
    suspend fun save(accountId: Long, card: DashboardCardModel)
    suspend fun getCards(): List<DashboardCardModel>
}
