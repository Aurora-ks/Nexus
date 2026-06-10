package com.nexus.game.wuwa

import com.nexus.core.storage.db.AccountEntity
import com.nexus.core.storage.db.dao.AccountDao
import com.nexus.game.wuwa.model.WuwaAccount

class RoomWuwaAccountStore(
    private val accountDao: AccountDao,
) : WuwaAccountStore {
    override suspend fun save(account: WuwaAccount): WuwaAccount {
        val now = System.currentTimeMillis()
        val existing = if (account.id != 0L) {
            accountDao.findById(account.id)
        } else {
            null
        } ?: accountDao.findByIdentity(
                gameId = account.gameId,
                userId = account.userId,
                roleId = account.roleId,
            )

        val entity = AccountEntity(
            id = existing?.id ?: account.id,
            gameId = account.gameId,
            userId = account.userId,
            roleId = account.roleId,
            serverId = account.serverId,
            roleName = account.roleName,
            serverName = account.serverName,
            localNickname = account.nickname ?: existing?.localNickname,
            headPhotoUrl = account.headPhotoUrl ?: existing?.headPhotoUrl,
            status = existing?.status ?: STATUS_ACTIVE,
            lastSyncAt = existing?.lastSyncAt,
            lastCheckInAt = existing?.lastCheckInAt,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        val persistedId = accountDao.upsert(entity)
        return entity.copy(id = persistedId).toDomainModel()
    }

    override suspend fun getAccount(accountId: Long): WuwaAccount? =
        accountDao.findById(accountId)?.toDomainModel()

    override suspend fun updateRemark(accountId: Long, nickname: String?): WuwaAccount? {
        val existing = accountDao.findById(accountId) ?: return null
        val updated = existing.copy(
            localNickname = nickname,
            updatedAt = System.currentTimeMillis(),
        )
        accountDao.upsert(updated)
        return updated.toDomainModel()
    }

    override suspend fun getAccounts(): List<WuwaAccount> =
        accountDao.getAccounts().map { it.toDomainModel() }

    override suspend fun delete(accountId: Long) {
        accountDao.deleteById(accountId)
    }

    private fun AccountEntity.toDomainModel(): WuwaAccount {
        return WuwaAccount(
            id = id,
            gameId = gameId,
            userId = userId,
            roleId = roleId,
            roleName = roleName,
            serverId = serverId,
            serverName = serverName,
            nickname = localNickname,
            headPhotoUrl = headPhotoUrl,
        )
    }

    private companion object {
        const val STATUS_ACTIVE = "ACTIVE"
    }
}
