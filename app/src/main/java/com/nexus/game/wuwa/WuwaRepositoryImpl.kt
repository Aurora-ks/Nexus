package com.nexus.game.wuwa

import com.nexus.core.model.AppError
import com.nexus.core.model.GameType
import com.nexus.core.model.OperationResult
import com.nexus.core.network.KuroHeaderProvider
import com.nexus.core.storage.secure.BoxAccessTokenKey
import com.nexus.core.storage.secure.TokenStore
import com.nexus.feature.account.AccountRepository
import com.nexus.feature.dashboard.DashboardRepository
import com.nexus.game.kuro.KuroTokenParser
import com.nexus.game.kuro.api.KuroRoleApi
import com.nexus.game.pgr.PgrRepository
import com.nexus.game.wuwa.api.WuwaAkiBoxApi
import com.nexus.game.wuwa.api.WuwaWidgetApi
import com.nexus.game.wuwa.model.DashboardCardModel
import com.nexus.game.wuwa.model.WuwaAccount
import com.nexus.game.wuwa.model.WuwaRefreshDataEnvelopeDto
import com.nexus.game.wuwa.model.WuwaRequestTokenEnvelopeDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class WuwaRepositoryImpl(
    private val roleApi: KuroRoleApi,
    private val widgetApi: WuwaWidgetApi,
    private val akiBoxApi: WuwaAkiBoxApi,
    private val pgrRepository: PgrRepository,
    private val headerProvider: KuroHeaderProvider,
    private val accountStore: WuwaAccountStore,
    private val snapshotStore: WuwaSnapshotStore,
    private val tokenStore: TokenStore,
) : WuwaRepository, AccountRepository, DashboardRepository {
    suspend fun getBoundAccounts(): List<WuwaAccount> = accountStore.getAccounts()

    suspend fun getCachedDashboardCards(): List<DashboardCardModel> = snapshotStore.getCards()

    suspend fun refreshAccountProfiles(): List<WuwaAccount> {
        return accountStore.getAccounts().map { account ->
            if (account.gameId != GameType.WUWA.gameId) return@map account
            val token = tokenStore.getBbsToken(account.id) ?: return@map account
            refreshHeadPhotoUrl(account, token)
        }
    }

    override suspend fun bindAccount(token: String, nickname: String?): OperationResult<WuwaAccount> {
        val userId = when (val parsed = KuroTokenParser.parseUserId(token)) {
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

        val headPhotoUrl = findHeadPhotoUrl(token = token, userId = userId, roleId = wuwaRole.roleId)

        val savedAccount = accountStore.save(
            WuwaAccount(
                gameId = wuwaRole.gameId,
                userId = wuwaRole.userId,
                roleId = wuwaRole.roleId,
                roleName = wuwaRole.roleName,
                serverId = wuwaRole.serverId,
                serverName = wuwaRole.serverName,
                nickname = nickname,
                headPhotoUrl = headPhotoUrl,
            ),
        )
        tokenStore.saveBbsToken(savedAccount.id, token)
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
        accountStore.getAccount(accountId)?.let { account ->
            tokenStore.deleteBoxAccessToken(account.toBoxAccessTokenKey())
        }
        tokenStore.deleteBbsToken(accountId)
        accountStore.delete(accountId)
        snapshotStore.delete(accountId)
        return OperationResult.Success(Unit)
    }

    override suspend fun syncAccounts(): OperationResult<List<DashboardCardModel>> {
        val cards = accountStore.getAccounts().mapNotNull { account ->
            val token = tokenStore.getBbsToken(account.id) ?: return@mapNotNull null
            val refreshedAccount = if (account.gameId == GameType.WUWA.gameId) {
                refreshHeadPhotoUrl(account, token)
            } else {
                account
            }
            fetchDashboardCard(refreshedAccount, token)?.also { snapshotStore.save(refreshedAccount.id, it) }
        }
        return OperationResult.Success(cards)
    }

    override suspend fun bindWuwaAccount(token: String, nickname: String?): OperationResult<WuwaAccount> {
        return bindAccount(token, nickname)
    }

    override suspend fun bindPgrAccount(token: String, nickname: String?): OperationResult<WuwaAccount> {
        val account = when (val result = pgrRepository.bindAccount(token, nickname)) {
            is OperationResult.Success -> result.value
            is OperationResult.Failure -> return result
        }
        val savedAccount = accountStore.save(account)
        tokenStore.saveBbsToken(savedAccount.id, token)
        fetchDashboardCard(savedAccount, token)?.let { snapshotStore.save(savedAccount.id, it) }

        return OperationResult.Success(savedAccount)
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
        return when (account.gameId) {
            GameType.WUWA.gameId -> fetchWuwaDashboardCard(account, token)
            GameType.PGR.gameId -> fetchPgrDashboardCard(account, token)
            else -> null
        }
    }

    private suspend fun fetchWuwaDashboardCard(
        account: WuwaAccount,
        token: String,
    ): DashboardCardModel? {
        when (refreshDashboardData(account, token)) {
            is OperationResult.Success -> Unit
            is OperationResult.Failure -> return null
        }

        val response = runCatching {
            widgetApi.getWidgetData(
                headers = headerProvider.webHeaders(token),
                gameId = account.gameId,
                roleId = account.roleId,
                serverId = account.serverId,
            )
        }.getOrNull() ?: return null

        if (!response.success) return null
        val data = response.data ?: return null
        return WuwaMappers.toDashboardCard(data)
    }

    private suspend fun fetchPgrDashboardCard(
        account: WuwaAccount,
        token: String,
    ): DashboardCardModel? {
        return pgrRepository.fetchDashboardCard(account, token)
    }

    private suspend fun findHeadPhotoUrl(
        token: String,
        userId: String,
        roleId: String,
    ): String? {
        val response = runCatching {
            roleApi.findRoleList(
                headers = headerProvider.nativeHeaders(token),
                fields = mapOf(
                    "queryUserId" to userId,
                    "gameId" to GameType.WUWA.gameId.toString(),
                ),
            )
        }.getOrNull() ?: return null

        if (!response.success) return null
        return response.data?.findHeadPhotoUrl(roleId) ?: response.data?.findAnyHeadPhotoUrl()
    }

    private suspend fun refreshHeadPhotoUrl(
        account: WuwaAccount,
        token: String,
    ): WuwaAccount {
        val headPhotoUrl = findHeadPhotoUrl(
            token = token,
            userId = account.userId,
            roleId = account.roleId,
        ) ?: return account

        if (headPhotoUrl == account.headPhotoUrl) return account
        return accountStore.save(account.copy(headPhotoUrl = headPhotoUrl))
    }

    private suspend fun refreshDashboardData(
        account: WuwaAccount,
        token: String,
    ): OperationResult<Unit> {
        val BoxAccessTokenKey = account.toBoxAccessTokenKey()
        val cachedBoxAccessToken = tokenStore.getBoxAccessToken(BoxAccessTokenKey)
        val BoxAccessToken = cachedBoxAccessToken ?: when (val result = requestBoxAccessToken(account, token)) {
            is OperationResult.Success -> result.value
            is OperationResult.Failure -> return result
        }

        return when (val refreshResult = submitRefreshData(account, BoxAccessToken)) {
            is OperationResult.Success -> refreshResult
            is OperationResult.Failure -> {
                if (cachedBoxAccessToken != null && refreshResult.error is AppError.AuthError) {
                    tokenStore.deleteBoxAccessToken(BoxAccessTokenKey)
                    when (val renewed = requestBoxAccessToken(account, token)) {
                        is OperationResult.Success -> submitRefreshData(account, renewed.value)
                        is OperationResult.Failure -> renewed
                    }
                } else {
                    refreshResult
                }
            }
        }
    }

    private suspend fun requestBoxAccessToken(
        account: WuwaAccount,
        token: String,
    ): OperationResult<String> {
        val response = runCatching {
            akiBoxApi.requestToken(
                headers = headerProvider.requestTokenHeaders(token),
                roleId = account.roleId,
                serverId = account.serverId,
                userId = account.userId,
            )
        }.getOrElse {
            return OperationResult.Failure(AppError.UnknownError(it.message ?: "获取 b-at 失败"))
        }

        if (!response.success) {
            return OperationResult.Failure(response.toAppError())
        }

        val payload = response.data
            ?: return OperationResult.Failure(AppError.ApiContractError("获取 b-at 响应缺少 data"))
        val boxAccessToken = runCatching { WuwaBoxAccessTokenParser.parse(payload) }
            .getOrElse {
                return OperationResult.Failure(AppError.ParseError(it.message ?: "解析 b-at 失败"))
            }

        tokenStore.saveBoxAccessToken(account.toBoxAccessTokenKey(), boxAccessToken)
        return OperationResult.Success(boxAccessToken)
    }

    private suspend fun submitRefreshData(
        account: WuwaAccount,
        boxAccessToken: String,
    ): OperationResult<Unit> {
        val response = runCatching {
            akiBoxApi.refreshData(
                headers = headerProvider.akiBoxHeaders(boxAccessToken),
                gameId = account.gameId,
                roleId = account.roleId,
                serverId = account.serverId,
            )
        }.getOrElse {
            return OperationResult.Failure(AppError.UnknownError(it.message ?: "刷新鸣潮数据失败"))
        }

        if (!response.success || response.data != true) {
            return OperationResult.Failure(response.toAppError(defaultMessage = "刷新鸣潮数据失败"))
        }
        return OperationResult.Success(Unit)
    }

    private fun WuwaAccount.toBoxAccessTokenKey(): BoxAccessTokenKey = BoxAccessTokenKey(
        userId = userId,
        roleId = roleId,
        gameId = gameId,
    )

    private fun JsonElement.findHeadPhotoUrl(roleId: String): String? {
        return when (this) {
            is JsonObject -> {
                val currentRoleId = this["roleId"]?.jsonPrimitive?.contentOrNull
                val headPhotoUrl = this["headPhotoUrl"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                if (currentRoleId == roleId && headPhotoUrl != null) {
                    headPhotoUrl
                } else {
                    values.firstNotNullOfOrNull { it.findHeadPhotoUrl(roleId) }
                }
            }
            is JsonArray -> firstNotNullOfOrNull { it.findHeadPhotoUrl(roleId) }
            else -> null
        }
    }

    private fun JsonElement.findAnyHeadPhotoUrl(): String? {
        return when (this) {
            is JsonObject -> {
                this["headPhotoUrl"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                    ?: values.firstNotNullOfOrNull { it.findAnyHeadPhotoUrl() }
            }
            is JsonArray -> firstNotNullOfOrNull { it.findAnyHeadPhotoUrl() }
            else -> null
        }
    }

    private fun WuwaRequestTokenEnvelopeDto.toAppError(): AppError {
        return mapEnvelopeError(code = code, message = msg, defaultMessage = "获取 b-at 失败")
    }

    private fun WuwaRefreshDataEnvelopeDto.toAppError(defaultMessage: String): AppError {
        return mapEnvelopeError(code = code, message = msg, defaultMessage = defaultMessage)
    }

    private fun mapEnvelopeError(
        code: Int,
        message: String,
        defaultMessage: String,
    ): AppError {
        val normalizedMessage = message.ifBlank { defaultMessage }
        if (code == 401 || code == 403 || normalizedMessage.containsAuthHint()) {
            return AppError.AuthError(normalizedMessage)
        }
        return AppError.UnknownError(normalizedMessage)
    }

    private fun String.containsAuthHint(): Boolean {
        return contains("token", ignoreCase = true) ||
                contains("鉴权") ||
                contains("认证") ||
                contains("登录") ||
                contains("失效")
    }
}
