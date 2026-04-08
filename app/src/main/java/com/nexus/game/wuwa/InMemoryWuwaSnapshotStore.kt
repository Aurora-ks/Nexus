package com.nexus.game.wuwa

import com.nexus.game.wuwa.model.DashboardCardModel

class InMemoryWuwaSnapshotStore : WuwaSnapshotStore {
    private val cardsByAccount = linkedMapOf<Long, DashboardCardModel>()

    override suspend fun save(accountId: Long, card: DashboardCardModel) {
        cardsByAccount[accountId] = card
    }

    override suspend fun getCards(): List<DashboardCardModel> = cardsByAccount.values.toList()
}
