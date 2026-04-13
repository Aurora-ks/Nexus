package com.nexus.game.wuwa

import com.nexus.core.model.AppError
import com.nexus.core.model.GameType
import com.nexus.core.model.OperationResult
import com.nexus.core.network.KuroHeaderProvider
import com.nexus.core.storage.secure.TokenStore
import com.nexus.feature.account.AccountRepository
import com.nexus.feature.dashboard.DashboardRepository
import com.nexus.game.wuwa.api.WuwaRoleApi
import com.nexus.game.wuwa.api.WuwaWidgetApi
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.WuwaAccount

class WuwaRepositoryImpl(
    private val roleApi: WuwaRoleApi,
    private val widgetApi: WuwaWidgetApi,
    private val headerProvider: KuroHeaderProvider,
    private val accountStore: WuwaAccountStore,
    private val snapshotStore: WuwaSnapshotStore,
    private val tokenStore: TokenStore,
) : WuwaRepository, AccountRepository, DashboardRepository {
    suspend fun getBoundAccounts(): List<WuwaAccount> = accountStore.getAccounts()

    suspend fun getCachedDashboardCards(): List<DashboardCardModel> = snapshotStore.getCards()

    override suspend fun bindAccount(token: String, nickname: String?): OperationResult<WuwaAccount> {
        val userId = when (val parsed = TokenParser.parseUserId(token)) {
            is OperationResult.Success -> parsed.value
            is OperationResult.Failure -> return parsed
        }

        val roleResponse = runCatching {
            roleApi.findUserDefaultRole(
                headers = headerProvider.nativeHeaders(token),
                queryUserId = userId,
            )
        }.getOrElse {
            return OperationResult.Failure(AppError.UnknownError(it.message ?: "绑定角色请求失败"))
        }

        if (!roleResponse.success) {
            return OperationResult.Failure(AppError.AuthError(roleResponse.msg))
        }

        val wuwaRole = roleResponse.data
            ?.defaultRoleList
            ?.firstOrNull { it.gameId == GameType.WUWA.gameId }
            ?: return OperationResult.Failure(AppError.ApiContractError("未找到鸣潮角色"))

        val savedAccount = accountStore.save(
            WuwaAccount(
                userId = wuwaRole.userId,
                roleId = wuwaRole.roleId,
                roleName = wuwaRole.roleName,
                serverId = wuwaRole.serverId,
                serverName = wuwaRole.serverName,
                nickname = nickname,
            ),
        )
        tokenStore.save(savedAccount.id, token)

        fetchDashboardCard(savedAccount, token)?.let { snapshotStore.save(savedAccount.id, it) }
        return OperationResult.Success(savedAccount)
    }

    override suspend fun updateAccountRemark(
        accountId: Long,
        nickname: String?,
    ): OperationResult<WuwaAccount> {
        val normalizedNickname = nickname?.trim()?.takeIf { it.isNotEmpty() }
        val updatedAccount = accountStore.updateRemark(accountId, normalizedNickname)
            ?: return OperationResult.Failure(AppError.UnknownError("未找到对应账号"))
        return OperationResult.Success(updatedAccount)
    }

    override suspend fun deleteAccount(accountId: Long): OperationResult<Unit> {
        accountStore.delete(accountId)
        tokenStore.delete(accountId)
        snapshotStore.delete(accountId)
        return OperationResult.Success(Unit)
    }

    override suspend fun syncAccounts(): OperationResult<List<DashboardCardModel>> {
        val cards = accountStore.getAccounts().mapNotNull { account ->
            val token = tokenStore.get(account.id) ?: return@mapNotNull null
            fetchDashboardCard(account, token)?.also { snapshotStore.save(account.id, it) }
        }
        return OperationResult.Success(cards)
    }

    override suspend fun bindWuwaAccount(token: String, nickname: String?): OperationResult<WuwaAccount> {
        return bindAccount(token, nickname)
    }

    override suspend fun updateWuwaAccountRemark(
        accountId: Long,
        nickname: String?,
    ): OperationResult<WuwaAccount> {
        return updateAccountRemark(accountId, nickname)
    }

    override suspend fun deleteWuwaAccount(accountId: Long): OperationResult<Unit> {
        return deleteAccount(accountId)
    }

    override suspend fun sync(): OperationResult<List<DashboardCardModel>> {
        return syncAccounts()
    }

    private suspend fun fetchDashboardCard(
        account: WuwaAccount,
        token: String,
    ): DashboardCardModel? {
        val response = runCatching {
            widgetApi.getWidgetData(
                headers = headerProvider.webHeaders(token),
                gameId = GameType.WUWA.gameId,
                roleId = account.roleId,
                serverId = account.serverId,
            )
        }.getOrNull() ?: return null

        if (!response.success) return null
        val data = response.data ?: return null
        return WuwaMappers.toDashboardCard(data)
    }
}
